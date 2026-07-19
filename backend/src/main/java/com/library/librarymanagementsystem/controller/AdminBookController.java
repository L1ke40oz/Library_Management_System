package com.library.librarymanagementsystem.controller;

import com.library.librarymanagementsystem.dto.ApiResponse;
import com.library.librarymanagementsystem.entity.Book;
import com.library.librarymanagementsystem.entity.User;
import com.library.librarymanagementsystem.mapper.BookTagMapper;
import com.library.librarymanagementsystem.mapper.UserMapper;
import com.library.librarymanagementsystem.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员图书管理接口
 * 所有接口需要传入 userId 参数用于验证管理员身份
 */
@CrossOrigin
@RestController
@RequestMapping("/api/admin/books")
public class AdminBookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BookTagMapper bookTagMapper;

    /**
     * 验证用户是否为管理员
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
        return null; // null 表示验证通过
    }

    /**
     * 获取所有图书列表（管理员视图，分页）
     */
    @GetMapping("/list")
    public ApiResponse<?> listBooks(@RequestParam Long userId,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        Map<String, Object> data = bookService.getBooksPaged(page, size);
        return ApiResponse.success("查询成功", data);
    }

    /**
     * 获取单本图书详情（含标签）
     */
    @GetMapping("/{bookId}")
    public ApiResponse<?> getBook(@PathVariable Long bookId, @RequestParam Long userId) {
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        Book book = bookService.getById(bookId);
        if (book == null) {
            return ApiResponse.error(404, "图书不存在");
        }

        List<String> tags = bookTagMapper.findTagsByBookId(bookId);
        Map<String, Object> data = new HashMap<>();
        data.put("book", book);
        data.put("tags", tags);
        return ApiResponse.success("查询成功", data);
    }

    /**
     * 添加图书
     */
    @PostMapping("/add")
    public ApiResponse<?> addBook(@RequestBody Map<String, Object> body) {
        Long userId = getLongValue(body, "userId");
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        Book book = new Book();
        book.setBookName((String) body.get("bookName"));
        book.setAuthor((String) body.get("author"));
        book.setPublisher((String) body.get("publisher"));
        book.setIsbn((String) body.get("isbn"));
        book.setCategory((String) body.get("category"));
        book.setDescription((String) body.get("description"));
        book.setTotalCount(getIntValue(body, "totalCount"));
        book.setBorrowCount(0);

        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) body.get("tags");

        return bookService.addBook(book, tags);
    }

    /**
     * 修改图书
     */
    @PutMapping("/update")
    public ApiResponse<?> updateBook(@RequestBody Map<String, Object> body) {
        Long userId = getLongValue(body, "userId");
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        Book book = new Book();
        book.setBookId(getLongValue(body, "bookId"));
        book.setBookName((String) body.get("bookName"));
        book.setAuthor((String) body.get("author"));
        book.setPublisher((String) body.get("publisher"));
        book.setIsbn((String) body.get("isbn"));
        book.setCategory((String) body.get("category"));
        book.setDescription((String) body.get("description"));
        book.setTotalCount(getIntValue(body, "totalCount"));

        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) body.get("tags");

        return bookService.updateBook(book, tags);
    }

    /**
     * 删除图书
     */
    @DeleteMapping("/delete/{bookId}")
    public ApiResponse<?> deleteBook(@PathVariable Long bookId, @RequestParam Long userId) {
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        return bookService.deleteBook(bookId);
    }

    /**
     * 搜索图书（管理员视图，支持书名、作者、ISBN，分页）
     */
    @GetMapping("/search")
    public ApiResponse<?> searchBooks(@RequestParam Long userId,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        Map<String, Object> data = bookService.searchBooksPaged(keyword, page, size);
        return ApiResponse.success("查询成功", data);
    }

    // ===== 工具方法 =====

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

    private Integer getIntValue(Map<String, Object> body, String key) {
        Object val = body.get(key);
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return Integer.valueOf(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
