package com.library.librarymanagementsystem.service;

import com.library.librarymanagementsystem.dto.ApiResponse;
import com.library.librarymanagementsystem.entity.Book;
import com.library.librarymanagementsystem.entity.Favorite;
import com.library.librarymanagementsystem.mapper.FavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    public ApiResponse<?> addFavorite(Long userId, Long bookId) {
        Favorite existing = favoriteMapper.findByUserAndBook(userId, bookId);
        if (existing != null) {
            return ApiResponse.error(400, "已收藏该图书");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setBookId(bookId);
        favoriteMapper.insert(favorite);

        return ApiResponse.success("收藏成功", null);
    }

    public ApiResponse<?> removeFavorite(Long userId, Long bookId) {
        int result = favoriteMapper.delete(userId, bookId);
        if (result > 0) {
            return ApiResponse.success("取消收藏成功", null);
        }
        return ApiResponse.error(404, "未找到收藏记录");
    }

    public boolean isFavorited(Long userId, Long bookId) {
        return favoriteMapper.findByUserAndBook(userId, bookId) != null;
    }

    public List<Book> getUserFavorites(Long userId) {
        return favoriteMapper.findBooksByUserId(userId);
    }
}
