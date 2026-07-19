package com.library.librarymanagementsystem.controller;

import com.library.librarymanagementsystem.dto.ApiResponse;
import com.library.librarymanagementsystem.entity.Borrow;
import com.library.librarymanagementsystem.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @PostMapping("/{bookId}")
    public ApiResponse<?> borrow(@RequestParam Long userId, @PathVariable Long bookId) {
        return borrowService.borrowBook(userId, bookId);
    }

    @PostMapping("/return/{borrowId}")
    public ApiResponse<?> returnBook(@RequestParam Long userId, @PathVariable Long borrowId) {
        return borrowService.returnBook(userId, borrowId);
    }

    @PostMapping("/renew/{borrowId}")
    public ApiResponse<?> renewBook(@RequestParam Long userId, @PathVariable Long borrowId) {
        return borrowService.renewBook(borrowId);
    }

    @GetMapping("/list")
    public ApiResponse<List<Borrow>> list(@RequestParam Long userId) {
        List<Borrow> borrows = borrowService.getUserBorrows(userId);
        return ApiResponse.success("查询成功", borrows);
    }
}
