package com.library.librarymanagementsystem.service;

import com.library.librarymanagementsystem.dto.ApiResponse;
import com.library.librarymanagementsystem.entity.Book;
import com.library.librarymanagementsystem.entity.Borrow;
import com.library.librarymanagementsystem.mapper.BookMapper;
import com.library.librarymanagementsystem.mapper.BorrowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BorrowService {

    @Autowired
    private BorrowMapper borrowMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private MessageService messageService;

    public ApiResponse<?> borrowBook(Long userId, Long bookId) {
        Book book = bookMapper.findById(bookId);
        if (book == null) {
            return ApiResponse.error(404, "图书不存在");
        }

        int available = book.getTotalCount() - book.getBorrowCount();
        if (available <= 0) {
            return ApiResponse.error(400, "该书已全部借出，暂无可借");
        }

        Borrow existing = borrowMapper.findActiveBorrow(userId, bookId);
        if (existing != null) {
            return ApiResponse.error(400, "您已借阅此书，请勿重复借阅");
        }

        Borrow borrow = new Borrow();
        borrow.setUserId(userId);
        borrow.setBookId(bookId);
        borrow.setBorrowDate(LocalDate.now());
        borrow.setDueDate(LocalDate.now().plusDays(30));
        borrow.setStatus("BORROWED");

        borrowMapper.insert(borrow);
        bookMapper.incrementBorrowCount(bookId);

        return ApiResponse.success("借阅成功，请在30天内归还", borrow);
    }

    public ApiResponse<?> returnBook(Long userId, Long borrowId) {
        List<Borrow> borrows = borrowMapper.findByUserId(userId);
        Borrow target = borrows.stream()
                .filter(b -> b.getBorrowId().equals(borrowId) && ("BORROWED".equals(b.getStatus()) || "OVERDUE".equals(b.getStatus())))
                .findFirst().orElse(null);

        if (target == null) {
            return ApiResponse.error(404, "未找到该借阅记录");
        }

        target.setStatus("RETURNED");
        target.setReturnDate(LocalDate.now());
        borrowMapper.updateStatus(target);
        bookMapper.decrementBorrowCount(target.getBookId());

        return ApiResponse.success("归还成功", null);
    }

    public List<Borrow> getUserBorrows(Long userId) {
        return borrowMapper.findByUserId(userId);
    }

    public Borrow getBorrowById(Long borrowId) {
        return borrowMapper.findById(borrowId);
    }

    public void updateDueDate(Long borrowId, LocalDate newDueDate) {
        borrowMapper.updateDueDate(borrowId, newDueDate);
    }

    public void updateBorrowDate(Long borrowId, LocalDate newBorrowDate) {
        borrowMapper.updateBorrowDate(borrowId, newBorrowDate);
    }

    // ===== 续借 =====

    public ApiResponse<?> renewBook(Long borrowId) {
        Borrow borrow = borrowMapper.findById(borrowId);
        if (borrow == null) {
            return ApiResponse.error(404, "借阅记录不存在");
        }
        if (!"BORROWED".equals(borrow.getStatus()) && !"OVERDUE".equals(borrow.getStatus())) {
            return ApiResponse.error(400, "该记录状态不允许续借");
        }

        // 续借30天（从当前到期日延长）
        LocalDate newDueDate = borrow.getDueDate().plusDays(30);
        borrowMapper.updateDueDate(borrowId, newDueDate);

        // 如果之前是逾期状态，续借后恢复为BORROWED
        if ("OVERDUE".equals(borrow.getStatus())) {
            borrow.setStatus("BORROWED");
            borrow.setReturnDate(null);
            borrowMapper.updateStatus(borrow);
        }

        return ApiResponse.success("续借成功，新到期日：" + newDueDate, null);
    }

    // ===== 管理员操作 =====

    /**
     * 管理员处理归还
     */
    public ApiResponse<?> adminReturnBook(Long borrowId) {
        Borrow borrow = borrowMapper.findById(borrowId);
        if (borrow == null) {
            return ApiResponse.error(404, "借阅记录不存在");
        }
        if ("RETURNED".equals(borrow.getStatus())) {
            return ApiResponse.error(400, "该书已归还");
        }

        borrow.setStatus("RETURNED");
        borrow.setReturnDate(LocalDate.now());
        borrowMapper.updateStatus(borrow);
        bookMapper.decrementBorrowCount(borrow.getBookId());

        return ApiResponse.success("归还处理成功", null);
    }

    /**
     * 分页获取所有借阅记录
     */
    public Map<String, Object> getAllBorrowsPaged(int page, int size) {
        int offset = (page - 1) * size;
        List<Borrow> list = borrowMapper.findAllPaged(offset, size);
        int total = borrowMapper.countAll();
        return buildPageResult(list, total, page, size);
    }

    /**
     * 按状态分页获取借阅记录
     */
    public Map<String, Object> getBorrowsByStatusPaged(String status, int page, int size) {
        int offset = (page - 1) * size;
        List<Borrow> list = borrowMapper.findByStatusPaged(status, offset, size);
        int total = borrowMapper.countByStatus(status);
        return buildPageResult(list, total, page, size);
    }

    /**
     * 搜索借阅记录（按用户名或书名）
     */
    public Map<String, Object> searchBorrowsPaged(String keyword, int page, int size) {
        int offset = (page - 1) * size;
        List<Borrow> list = borrowMapper.searchPaged(keyword, offset, size);
        int total = borrowMapper.countByKeyword(keyword);
        return buildPageResult(list, total, page, size);
    }

    /**
     * 获取所有逾期记录
     */
    public List<Borrow> getOverdueList() {
        return borrowMapper.findOverdue(LocalDate.now());
    }

    /**
     * 检查并标记逾期，同时发送站内消息提醒
     */
    public ApiResponse<?> checkAndNotifyOverdue() {
        LocalDate today = LocalDate.now();

        // 获取逾期但状态还是BORROWED的记录
        List<Borrow> overdueList = borrowMapper.findOverdue(today);

        // 批量标记为逾期
        int updated = borrowMapper.markOverdue(today);

        // 给每个逾期用户发送站内消息
        int sentCount = 0;
        String lastError = null;
        for (Borrow borrow : overdueList) {
            try {
                String title = "【逾期提醒】您借阅的图书已逾期";
                String content = String.format(
                    "您好，您借阅的《%s》（作者：%s）已于 %s 到期，目前已逾期。请尽快归还，以免影响您的借阅权限。",
                    borrow.getBookName() != null ? borrow.getBookName() : "未知",
                    borrow.getAuthor() != null ? borrow.getAuthor() : "未知",
                    borrow.getDueDate().toString()
                );
                messageService.sendMessage(borrow.getUserId(), title, content, "OVERDUE");
                sentCount++;
            } catch (Exception e) {
                lastError = e.getMessage();
            }
        }

        String msg = "逾期检查完成，已标记 " + updated + " 条逾期记录，成功发送 " + sentCount + " 条提醒";
        if (sentCount == 0 && lastError != null) {
            msg += "（发送失败原因：" + lastError + "）";
        }
        return ApiResponse.success(msg, overdueList.size());
    }

    private Map<String, Object> buildPageResult(List<Borrow> list, int total, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }
}
