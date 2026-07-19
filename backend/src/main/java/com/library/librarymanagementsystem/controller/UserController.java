package com.library.librarymanagementsystem.controller;

import com.library.librarymanagementsystem.dto.ApiResponse;
import com.library.librarymanagementsystem.entity.User;
import com.library.librarymanagementsystem.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/info")
    public ApiResponse<?> getUserInfo(@RequestParam Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("email", user.getEmail());
        info.put("role", user.getRole());
        info.put("createdAt", user.getCreatedAt());
        return ApiResponse.success("查询成功", info);
    }

    @PutMapping("/update")
    public ApiResponse<?> updateUser(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String username = (String) body.get("username");
        String email = (String) body.get("email");

        User user = userMapper.findById(userId);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        // 检查用户名是否被占用
        if (username != null && !username.equals(user.getUsername())) {
            User existing = userMapper.findByUsername(username);
            if (existing != null) {
                return ApiResponse.error(400, "用户名已被占用");
            }
            user.setUsername(username);
        }

        // 检查邮箱是否被占用
        if (email != null && !email.equals(user.getEmail())) {
            User existing = userMapper.findByEmail(email);
            if (existing != null) {
                return ApiResponse.error(400, "邮箱已被占用");
            }
            user.setEmail(email);
        }

        userMapper.updateUser(user);
        return ApiResponse.success("修改成功", null);
    }

    /**
     * 用户修改密码
     */
    @PutMapping("/change-password")
    public ApiResponse<?> changePassword(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String oldPassword = (String) body.get("oldPassword");
        String newPassword = (String) body.get("newPassword");

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return ApiResponse.error(400, "请输入原密码");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ApiResponse.error(400, "请输入新密码");
        }
        if (newPassword.length() < 6) {
            return ApiResponse.error(400, "新密码长度不能少于6位");
        }

        User user = userMapper.findById(userId);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        if (!user.getPassword().equals(oldPassword)) {
            return ApiResponse.error(401, "原密码错误");
        }

        if (oldPassword.equals(newPassword)) {
            return ApiResponse.error(400, "新密码不能与原密码相同");
        }

        userMapper.updatePassword(userId, newPassword);
        return ApiResponse.success("密码修改成功", null);
    }
}
