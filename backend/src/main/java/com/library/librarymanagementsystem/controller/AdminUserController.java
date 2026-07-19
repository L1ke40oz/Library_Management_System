package com.library.librarymanagementsystem.controller;

import com.library.librarymanagementsystem.dto.ApiResponse;
import com.library.librarymanagementsystem.entity.Borrow;
import com.library.librarymanagementsystem.entity.User;
import com.library.librarymanagementsystem.mapper.*;
import com.library.librarymanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员读者管理接口
 */
@CrossOrigin
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BorrowMapper borrowMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private ReaderMapper readerMapper;

    @Autowired
    private AdminMapper adminMapper;

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
     * 获取用户列表（分页，可按角色筛选）
     */
    @GetMapping("/list")
    public ApiResponse<?> listUsers(@RequestParam Long userId,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    @RequestParam(required = false) String role) {
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        int offset = (page - 1) * size;
        List<User> list;
        int total;

        if (role != null && !role.trim().isEmpty()) {
            list = userMapper.findByRolePaged(role.trim(), offset, size);
            total = userMapper.countByRole(role.trim());
        } else {
            list = userMapper.findAllPaged(offset, size);
            total = userMapper.countAll();
        }

        // 清除密码字段
        list.forEach(u -> u.setPassword(null));

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        data.put("totalPages", (int) Math.ceil((double) total / size));
        return ApiResponse.success("查询成功", data);
    }

    /**
     * 搜索用户（按用户名或邮箱）
     */
    @GetMapping("/search")
    public ApiResponse<?> searchUsers(@RequestParam Long userId,
                                      @RequestParam String keyword,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        int offset = (page - 1) * size;
        List<User> list = userMapper.searchPaged(keyword.trim(), offset, size);
        int total = userMapper.countByKeyword(keyword.trim());

        list.forEach(u -> u.setPassword(null));

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        data.put("totalPages", (int) Math.ceil((double) total / size));
        return ApiResponse.success("查询成功", data);
    }

    /**
     * 获取单个用户详情
     */
    @GetMapping("/{targetId}")
    public ApiResponse<?> getUserDetail(@PathVariable Long targetId, @RequestParam Long userId) {
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        User target = userMapper.findById(targetId);
        if (target == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        target.setPassword(null);
        return ApiResponse.success("查询成功", target);
    }

    /**
     * 查看用户的借阅记录
     */
    @GetMapping("/{targetId}/borrows")
    public ApiResponse<?> getUserBorrows(@PathVariable Long targetId, @RequestParam Long userId) {
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        List<Borrow> borrows = borrowMapper.findByUserId(targetId);
        return ApiResponse.success("查询成功", borrows);
    }

    /**
     * 管理员创建用户（注册读者）
     */
    @PostMapping("/add")
    public ApiResponse<?> addUser(@RequestBody Map<String, Object> body) {
        Long adminId = getLongValue(body, "userId");
        ApiResponse<?> check = checkAdmin(adminId);
        if (check != null) return check;

        String username = (String) body.get("username");
        String email = (String) body.get("email");
        String password = (String) body.get("password");
        String role = (String) body.get("role");

        if (username == null || username.trim().isEmpty()) {
            return ApiResponse.error(400, "用户名不能为空");
        }
        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.error(400, "邮箱不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return ApiResponse.error(400, "密码不能为空");
        }

        // 检查用户名是否已存在
        if (userMapper.findByUsername(username.trim()) != null) {
            return ApiResponse.error(400, "用户名已存在");
        }
        // 检查邮箱是否已存在
        if (userMapper.findByEmail(email.trim()) != null) {
            return ApiResponse.error(400, "邮箱已被注册");
        }

        User newUser = new User();
        newUser.setUsername(username.trim());
        newUser.setEmail(email.trim());
        newUser.setPassword(password);
        newUser.setRole(role != null && !role.trim().isEmpty() ? role.trim() : "USER");

        int result = userMapper.insertUser(newUser);
        if (result > 0) {
            // 同步写入角色子表
            userService.syncRoleTable(newUser);
            newUser.setPassword(null);
            return ApiResponse.success("创建用户成功", newUser);
        }
        return ApiResponse.error(500, "创建用户失败");
    }

    /**
     * 管理员修改用户信息
     */
    @PutMapping("/update")
    public ApiResponse<?> updateUser(@RequestBody Map<String, Object> body) {
        Long adminId = getLongValue(body, "userId");
        ApiResponse<?> check = checkAdmin(adminId);
        if (check != null) return check;

        Long targetId = getLongValue(body, "targetId");
        if (targetId == null) {
            return ApiResponse.error(400, "目标用户ID不能为空");
        }

        User target = userMapper.findById(targetId);
        if (target == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        String username = (String) body.get("username");
        String email = (String) body.get("email");
        String role = (String) body.get("role");

        // 检查用户名冲突
        if (username != null && !username.trim().isEmpty() && !username.trim().equals(target.getUsername())) {
            if (userMapper.findByUsername(username.trim()) != null) {
                return ApiResponse.error(400, "用户名已被占用");
            }
            target.setUsername(username.trim());
        }

        // 检查邮箱冲突
        if (email != null && !email.trim().isEmpty() && !email.trim().equals(target.getEmail())) {
            if (userMapper.findByEmail(email.trim()) != null) {
                return ApiResponse.error(400, "邮箱已被占用");
            }
            target.setEmail(email.trim());
        }

        // 更新角色
        if (role != null && !role.trim().isEmpty()) {
            String newRole = role.trim();
            if (!newRole.equals(target.getRole())) {
                // 角色发生变化，同步迁移子表
                target.setRole(newRole);
                userService.migrateRoleTable(target, newRole);
            }
        }

        userMapper.updateUserFull(target);
        target.setPassword(null);
        return ApiResponse.success("修改成功", target);
    }

    /**
     * 管理员重置用户密码
     */
    @PutMapping("/reset-password")
    public ApiResponse<?> resetPassword(@RequestBody Map<String, Object> body) {
        Long adminId = getLongValue(body, "userId");
        ApiResponse<?> check = checkAdmin(adminId);
        if (check != null) return check;

        Long targetId = getLongValue(body, "targetId");
        String newPassword = (String) body.get("newPassword");

        if (targetId == null) {
            return ApiResponse.error(400, "目标用户ID不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ApiResponse.error(400, "新密码不能为空");
        }

        User target = userMapper.findById(targetId);
        if (target == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        userMapper.updatePassword(targetId, newPassword);
        return ApiResponse.success("密码重置成功", null);
    }

    /**
     * 管理员修改用户角色（权限分配）
     */
    @PutMapping("/role")
    public ApiResponse<?> updateRole(@RequestBody Map<String, Object> body) {
        Long adminId = getLongValue(body, "userId");
        ApiResponse<?> check = checkAdmin(adminId);
        if (check != null) return check;

        Long targetId = getLongValue(body, "targetId");
        String role = (String) body.get("role");

        if (targetId == null) {
            return ApiResponse.error(400, "目标用户ID不能为空");
        }
        if (role == null || role.trim().isEmpty()) {
            return ApiResponse.error(400, "角色不能为空");
        }
        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            return ApiResponse.error(400, "角色只能是 USER 或 ADMIN");
        }

        // 不能修改自己的角色
        if (targetId.equals(adminId)) {
            return ApiResponse.error(400, "不能修改自己的角色");
        }

        User target = userMapper.findById(targetId);
        if (target == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        userMapper.updateRole(targetId, role.trim());
        // 同步迁移角色子表数据
        userService.migrateRoleTable(target, role.trim());
        return ApiResponse.success("角色修改成功", null);
    }

    /**
     * 管理员删除用户
     */
    @DeleteMapping("/delete/{targetId}")
    public ApiResponse<?> deleteUser(@PathVariable Long targetId, @RequestParam Long userId) {
        ApiResponse<?> check = checkAdmin(userId);
        if (check != null) return check;

        if (targetId.equals(userId)) {
            return ApiResponse.error(400, "不能删除自己的账号");
        }

        User target = userMapper.findById(targetId);
        if (target == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        try {
            // 先删除所有关联表数据，避免外键约束报错（表不存在时忽略）
            try { borrowMapper.deleteByUserId(targetId); } catch (Exception ignored) {}
            try { favoriteMapper.deleteByUserId(targetId); } catch (Exception ignored) {}
            try { commentMapper.deleteByUserId(targetId); } catch (Exception ignored) {}
            try { messageMapper.deleteByUserId(targetId); } catch (Exception ignored) {}
            try { readerMapper.deleteByUserId(targetId); } catch (Exception ignored) {}
            try { adminMapper.deleteByUserId(targetId); } catch (Exception ignored) {}

            userMapper.deleteUser(targetId);
            return ApiResponse.success("删除用户成功", null);
        } catch (Exception e) {
            return ApiResponse.error(500, "删除失败：" + e.getMessage());
        }
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
