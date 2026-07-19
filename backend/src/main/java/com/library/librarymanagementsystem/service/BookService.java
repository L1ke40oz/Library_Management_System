package com.library.librarymanagementsystem.service;

import com.library.librarymanagementsystem.dto.ApiResponse;
import com.library.librarymanagementsystem.entity.Book;
import com.library.librarymanagementsystem.mapper.BookMapper;
import com.library.librarymanagementsystem.mapper.BookTagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private BookTagMapper bookTagMapper;

    public Book getById(Long bookId) {
        return bookMapper.findById(bookId);
    }

    public List<Book> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return bookMapper.findAll();
        }
        return bookMapper.searchByKeyword(keyword.trim());
    }

    public List<Book> findByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return bookMapper.findAll();
        }
        return bookMapper.findByCategory(category.trim());
    }

    public List<String> getAllCategories() {
        return bookMapper.findAllCategories();
    }

    public List<Book> findByTag(String tag) {
        return bookMapper.findByTag(tag);
    }

    public List<Book> getRandomBooks(int count) {
        return bookMapper.findRandom(count);
    }

    // ===== 管理员操作 =====

    @Transactional
    public ApiResponse<?> addBook(Book book, List<String> tags) {
        if (book.getBookName() == null || book.getBookName().trim().isEmpty()) {
            return ApiResponse.error(400, "书名不能为空");
        }

        // 检查ISBN是否重复
        if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
            Book existing = bookMapper.findByIsbn(book.getIsbn().trim());
            if (existing != null) {
                return ApiResponse.error(400, "ISBN已存在，不能重复添加");
            }
        }

        if (book.getTotalCount() == null) {
            book.setTotalCount(0);
        }
        if (book.getBorrowCount() == null) {
            book.setBorrowCount(0);
        }

        int result = bookMapper.insertBook(book);
        if (result > 0) {
            // 插入标签
            if (tags != null && !tags.isEmpty()) {
                for (String tag : tags) {
                    if (tag != null && !tag.trim().isEmpty()) {
                        bookTagMapper.insertTag(book.getBookId(), tag.trim());
                    }
                }
            }
            return ApiResponse.success("添加图书成功", book);
        }
        return ApiResponse.error(500, "添加图书失败");
    }

    @Transactional
    public ApiResponse<?> updateBook(Book book, List<String> tags) {
        if (book.getBookId() == null) {
            return ApiResponse.error(400, "图书ID不能为空");
        }

        Book existing = bookMapper.findById(book.getBookId());
        if (existing == null) {
            return ApiResponse.error(404, "图书不存在");
        }

        // 检查ISBN是否与其他书重复
        if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
            Book isbnBook = bookMapper.findByIsbn(book.getIsbn().trim());
            if (isbnBook != null && !isbnBook.getBookId().equals(book.getBookId())) {
                return ApiResponse.error(400, "ISBN已被其他图书使用");
            }
        }

        int result = bookMapper.updateBook(book);
        if (result > 0) {
            // 更新标签：先删除旧标签，再插入新标签
            if (tags != null) {
                bookTagMapper.deleteByBookId(book.getBookId());
                for (String tag : tags) {
                    if (tag != null && !tag.trim().isEmpty()) {
                        bookTagMapper.insertTag(book.getBookId(), tag.trim());
                    }
                }
            }
            return ApiResponse.success("修改图书成功", book);
        }
        return ApiResponse.error(500, "修改图书失败");
    }

    @Transactional
    public ApiResponse<?> deleteBook(Long bookId) {
        Book existing = bookMapper.findById(bookId);
        if (existing == null) {
            return ApiResponse.error(404, "图书不存在");
        }

        // 检查是否有未归还的借阅
        if (existing.getBorrowCount() != null && existing.getBorrowCount() > 0) {
            return ApiResponse.error(400, "该图书尚有未归还的借阅记录，无法删除");
        }

        // 先删除标签
        bookTagMapper.deleteByBookId(bookId);
        // 再删除图书
        int result = bookMapper.deleteBook(bookId);
        if (result > 0) {
            return ApiResponse.success("删除图书成功", null);
        }
        return ApiResponse.error(500, "删除图书失败");
    }

    public List<Book> getAllBooks() {
        return bookMapper.findAll();
    }

    /**
     * 分页获取所有图书
     */
    public Map<String, Object> getBooksPaged(int page, int size) {
        int offset = (page - 1) * size;
        List<Book> list = bookMapper.findPage(offset, size);
        int total = bookMapper.countAll();
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    /**
     * 分页搜索图书
     */
    public Map<String, Object> searchBooksPaged(String keyword, int page, int size) {
        int offset = (page - 1) * size;
        List<Book> list;
        int total;
        if (keyword == null || keyword.trim().isEmpty()) {
            list = bookMapper.findPage(offset, size);
            total = bookMapper.countAll();
        } else {
            list = bookMapper.searchByKeywordPage(keyword.trim(), offset, size);
            total = bookMapper.countByKeyword(keyword.trim());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }
}
