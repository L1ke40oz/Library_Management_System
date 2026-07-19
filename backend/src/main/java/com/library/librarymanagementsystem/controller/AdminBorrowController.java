package com.library.librarymanagementsystem.controller;

import com.library.librarymanagementsystem.dto.ApiResponse;
import com.library.librarymanagementsystem.entity.Borrow;
import com.library.librarymanagementsystem.entity.User;
import com.library.librarymanagementsystem.mapper.UserMapper;
import com.library.librarymanagementsystem.service.BorrowService;
import com.library.librarymanagementsystem.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员借阅管理接口
 */
@CrossOrigin
@RestController
@RequestMapping("/api/admin/borrow")
public class AdminBorrowController {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 验证管理员身份
     */
    private ApiResponse<?> checkAdmin(Long userId) {
        if (userId == null) {
            return ApiResponse.error(401, "未登录");
        }
        User user = userMapper.findById(userId);
        if (user == null) {
            return ApiResponse.error(401, "用户不存在");
        }
        if (!"ADMIN".equals(user.getRole())) {
            return ApiResponse.error(403, "权限不足，仅管理员可操作");
        }
        return null;
    }

    /**
     * 获取所有借阅记录（分页）
     */
    @GetMapping("/list")
    public ApiResponse<?> listBorrows(@RequestParam Long userId,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      @RequestParam(required = false) String status) {
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        Map<String, Object> data;
        if (status != null && !status.trim().isEmpty()) {
            data = borrowService.getBorrowsByStatusPaged(status.trim(), page, size);
        } else {
            data = borrowService.getAllBorrowsPaged(page, size);
        }
        return ApiResponse.success("查询成功", data);
    }

    /**
     * 搜索借阅记录（按用户名或书名）
     */
    @GetMapping("/search")
    public ApiResponse<?> searchBorrows(@RequestParam Long userId,
                                        @RequestParam String keyword,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        Map<String, Object> data = borrowService.searchBorrowsPaged(keyword.trim(), page, size);
        return ApiResponse.success("查询成功", data);
    }

    /**
     * 管理员处理归还
     */
    @PostMapping("/return/{borrowId}")
    public ApiResponse<?> returnBook(@PathVariable Long borrowId, @RequestParam Long userId) {
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        return borrowService.adminReturnBook(borrowId);
    }

    /**
     * 管理员处理续借
     */
    @PostMapping("/renew/{borrowId}")
    public ApiResponse<?> renewBook(@PathVariable Long borrowId, @RequestParam Long userId) {
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        return borrowService.renewBook(borrowId);
    }

    /**
     * 管理员修改到期日期（可同时修改借阅日期）
     */
    @PutMapping("/change-due-date")
    public ApiResponse<?> changeDueDate(@RequestBody Map<String, Object> body) {
        Long adminUserId = getLongValue(body, "userId");
        ApiResponse<?> check = checkAdmin(adminUserId);
        if (check != null) return check;

        Long borrowId = getLongValue(body, "borrowId");
        String dueDateStr = (String) body.get("dueDate");
        String borrowDateStr = (String) body.get("borrowDate");

        if (borrowId == null || dueDateStr == null || dueDateStr.trim().isEmpty()) {
            return ApiResponse.error(400, "借阅ID和到期日期不能为空");
        }

        try {
            com.library.librarymanagementsystem.entity.Borrow borrow = borrowService.getBorrowById(borrowId);
            if (borrow == null) {
                return ApiResponse.error(404, "借阅记录不存在");
            }

            java.time.LocalDate newDueDate = java.time.LocalDate.parse(dueDateStr.trim());

            // 如果提供了借阅日期，也一起修改
            java.time.LocalDate effectiveBorrowDate = borrow.getBorrowDate();
            if (borrowDateStr != null && !borrowDateStr.trim().isEmpty()) {
                effectiveBorrowDate = java.time.LocalDate.parse(borrowDateStr.trim());
            }

            // 校验：到期日期不能在借阅日期之前
            if (newDueDate.isBefore(effectiveBorrowDate)) {
                return ApiResponse.error(400, "到期日期不能在借阅日期之前");
            }

            if (borrowDateStr != null && !borrowDateStr.trim().isEmpty()) {
                borrowService.updateBorrowDate(borrowId, effectiveBorrowDate);
            }

            borrowService.updateDueDate(borrowId, newDueDate);
            return ApiResponse.success("日期修改成功，新到期日：" + newDueDate, null);
        } catch (java.time.format.DateTimeParseException e) {
            return ApiResponse.error(400, "日期格式错误，请使用 yyyy-MM-dd 格式");
        }
    }

    /**
     * 获取逾期列表
     */
    @GetMapping("/overdue")
    public ApiResponse<?> getOverdueList(@RequestParam Long userId) {
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        List<Borrow> list = borrowService.getOverdueList();
        return ApiResponse.success("查询成功", list);
    }

    /**
     * 检查逾期并发送站内提醒
     */
    @PostMapping("/check-overdue")
    public ApiResponse<?> checkOverdue(@RequestParam Long userId) {
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        return borrowService.checkAndNotifyOverdue();
    }

    /**
     * 管理员手动给用户发送消息
     */
    @PostMapping("/send-message")
    public ApiResponse<?> sendMessage(@RequestBody Map<String, Object> body) {
        Long adminUserId = getLongValue(body, "userId");
        ApiResponse<?> check = checkAdmin(adminUserId);
        if (check != null) return check;

        Long targetUserId = getLongValue(body, "targetUserId");
        String title = (String) body.get("title");
        String content = (String) body.get("content");

        if (targetUserId == null || title == null || title.trim().isEmpty()) {
            return ApiResponse.error(400, "目标用户和标题不能为空");
        }

        messageService.sendMessage(targetUserId, title, content, "NOTICE");
        return ApiResponse.success("消息发送成功", null);
    }

    private Long getLongValue(Map<String, Object> body, String key) {
        Object val = body.get(key);
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            return Long.valueOf(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
