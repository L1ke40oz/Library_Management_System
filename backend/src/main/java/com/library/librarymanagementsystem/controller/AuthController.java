package com.library.librarymanagementsystem.controller;

import com.library.librarymanagementsystem.dto.ApiResponse;
import com.library.librarymanagementsystem.dto.LoginRequest;
import com.library.librarymanagementsystem.dto.RegisterRequest;
import com.library.librarymanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin // Important for Vue frontend (localhost:5173 / localhost:8080)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
