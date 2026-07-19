package com.library.librarymanagementsystem.controller;

import com.library.librarymanagementsystem.dto.ApiResponse;
import com.library.librarymanagementsystem.entity.Book;
import com.library.librarymanagementsystem.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/{bookId}")
    public ApiResponse<?> add(@RequestParam Long userId, @PathVariable Long bookId) {
        return favoriteService.addFavorite(userId, bookId);
    }

    @DeleteMapping("/{bookId}")
    public ApiResponse<?> remove(@RequestParam Long userId, @PathVariable Long bookId) {
        return favoriteService.removeFavorite(userId, bookId);
    }

    @GetMapping("/check/{bookId}")
    public ApiResponse<Boolean> check(@RequestParam Long userId, @PathVariable Long bookId) {
        boolean result = favoriteService.isFavorited(userId, bookId);
        return ApiResponse.success("查询成功", result);
    }

    @GetMapping("/list")
    public ApiResponse<List<Book>> list(@RequestParam Long userId) {
        List<Book> books = favoriteService.getUserFavorites(userId);
        return ApiResponse.success("查询成功", books);
    }
}
