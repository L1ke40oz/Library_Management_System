package com.library.librarymanagementsystem.controller;

import com.library.librarymanagementsystem.dto.ApiResponse;
import com.library.librarymanagementsystem.entity.Book;
import com.library.librarymanagementsystem.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/{id}")
    public ApiResponse<Book> getBookDetail(@PathVariable("id") Long id) {
        Book book = bookService.getById(id);
        if (book == null) {
            return ApiResponse.error(404, "图书不存在");
        }
        return ApiResponse.success("查询成功", book);
    }

    @GetMapping("/search")
    public ApiResponse<List<Book>> search(@RequestParam(required = false) String keyword) {
        List<Book> books = bookService.search(keyword);
        return ApiResponse.success("查询成功", books);
    }

    @GetMapping("/category")
    public ApiResponse<List<Book>> getByCategory(@RequestParam String name) {
        List<Book> books = bookService.findByCategory(name);
        return ApiResponse.success("查询成功", books);
    }

    @GetMapping("/categories")
    public ApiResponse<List<String>> getCategories() {
        List<String> categories = bookService.getAllCategories();
        return ApiResponse.success("查询成功", categories);
    }

    @GetMapping("/tag")
    public ApiResponse<List<Book>> getByTag(@RequestParam String name) {
        List<Book> books = bookService.findByTag(name);
        return ApiResponse.success("查询成功", books);
    }

    @GetMapping("/random")
    public ApiResponse<List<Book>> getRandomBooks(@RequestParam(defaultValue = "3") int count) {
        List<Book> books = bookService.getRandomBooks(count);
        return ApiResponse.success("查询成功", books);
    }
}
