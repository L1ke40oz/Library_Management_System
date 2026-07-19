package com.library.librarymanagementsystem.controller;

import com.library.librarymanagementsystem.dto.ApiResponse;
import com.library.librarymanagementsystem.entity.Message;
import com.library.librarymanagementsystem.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取用户所有消息
     */
    @GetMapping("/list")
    public ApiResponse<?> getMessages(@RequestParam Long userId) {
        List<Message> messages = messageService.getUserMessages(userId);
        return ApiResponse.success("查询成功", messages);
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public ApiResponse<?> getUnreadCount(@RequestParam Long userId) {
        int count = messageService.getUnreadCount(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return ApiResponse.success("查询成功", data);
    }

    /**
     * 标记单条消息为已读
     */
    @PutMapping("/read/{messageId}")
    public ApiResponse<?> markAsRead(@PathVariable Long messageId) {
        messageService.markAsRead(messageId);
        return ApiResponse.success("已标记为已读", null);
    }

    /**
     * 标记所有消息为已读
     */
    @PutMapping("/read-all")
    public ApiResponse<?> markAllAsRead(@RequestParam Long userId) {
        messageService.markAllAsRead(userId);
        return ApiResponse.success("已全部标记为已读", null);
    }
}
