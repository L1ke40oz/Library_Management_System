package com.library.librarymanagementsystem.service;

import com.library.librarymanagementsystem.dto.ApiResponse;
import com.library.librarymanagementsystem.dto.LoginRequest;
import com.library.librarymanagementsystem.dto.RegisterRequest;
import com.library.librarymanagementsystem.entity.Admin;
import com.library.librarymanagementsystem.entity.Reader;
import com.library.librarymanagementsystem.entity.User;
import com.library.librarymanagementsystem.mapper.AdminMapper;
import com.library.librarymanagementsystem.mapper.ReaderMapper;
import com.library.librarymanagementsystem.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReaderMapper readerMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Transactional
    public ApiResponse<?> register(RegisterRequest request) {
        if (request.getUsername() == null || request.getPassword() == null || request.getEmail() == null) {
            return ApiResponse.error(400, "用户名、邮箱或密码不能为空");
        }

        User existingUser = userMapper.findByUsername(request.getUsername());
        if (existingUser != null) {
            return ApiResponse.error(400, "用户名已存在");
        }

        User existingEmail = userMapper.findByEmail(request.getEmail());
        if (existingEmail != null) {
            return ApiResponse.error(400, "邮箱已被注册");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        // In real app, must use PBKDF2, BCrypt or Argon2. Using simple text for demo.
        newUser.setPassword(request.getPassword());
        newUser.setRole("USER");

        int result = userMapper.insertUser(newUser);
        if (result > 0) {
            // 同步写入 reader 子表
            syncRoleTable(newUser);
            return ApiResponse.success("注册成功", null);
        } else {
            return ApiResponse.error(500, "注册失败，服务器内部错误");
        }
    }

    /**
     * 根据用户角色同步写入对应的子表（reader / admin）
     */
    public void syncRoleTable(User user) {
        if ("USER".equals(user.getRole())) {
            Reader reader = new Reader();
            reader.setUserId(user.getId());
            reader.setReaderName(user.getUsername());
            reader.setContact(user.getEmail());
            reader.setStatus("ACTIVE");
            readerMapper.insert(reader);
        } else if ("ADMIN".equals(user.getRole())) {
            Admin admin = new Admin();
            admin.setUserId(user.getId());
            admin.setAdminName(user.getUsername());
            admin.setUsername(user.getUsername());
            admin.setPassword(user.getPassword());
            admin.setPermission("NORMAL");
            adminMapper.insert(admin);
        }
    }

    /**
     * 角色变更时迁移子表数据
     */
    @Transactional
    public void migrateRoleTable(User user, String newRole) {
        // 删除旧角色子表记录
        readerMapper.deleteByUserId(user.getId());
        adminMapper.deleteByUserId(user.getId());

        // 插入新角色子表记录
        if ("USER".equals(newRole)) {
            Reader reader = new Reader();
            reader.setUserId(user.getId());
            reader.setReaderName(user.getUsername());
            reader.setContact(user.getEmail());
            reader.setStatus("ACTIVE");
            readerMapper.insert(reader);
        } else if ("ADMIN".equals(newRole)) {
            Admin admin = new Admin();
            admin.setUserId(user.getId());
            admin.setAdminName(user.getUsername());
            admin.setUsername(user.getUsername());
            admin.setPassword(user.getPassword());
            admin.setPermission("NORMAL");
            adminMapper.insert(admin);
        }
    }

    public ApiResponse<?> login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ApiResponse.error(400, "用户名/邮箱或密码不能为空");
        }

        // 支持用户名或邮箱登录
        String input = request.getUsername().trim();
        User user;
        if (input.contains("@")) {
            user = userMapper.findByEmail(input);
        } else {
            user = userMapper.findByUsername(input);
        }

        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            return ApiResponse.error(401, "密码错误");
        }

        // Mock token generation
        String token = UUID.randomUUID().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());

        return ApiResponse.success("登录成功", data);
    }
}
