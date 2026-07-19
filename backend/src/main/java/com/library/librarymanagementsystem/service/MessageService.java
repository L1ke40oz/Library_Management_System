package com.library.librarymanagementsystem.service;

import com.library.librarymanagementsystem.entity.Message;
import com.library.librarymanagementsystem.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    /**
     * 发送站内消息
     */
    public void sendMessage(Long userId, String title, String content, String type) {
        Message msg = new Message();
        msg.setUserId(userId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setType(type);
        messageMapper.insert(msg);
    }

    /**
     * 获取用户所有消息
     */
    public List<Message> getUserMessages(Long userId) {
        return messageMapper.findByUserId(userId);
    }

    /**
     * 获取用户未读消息
     */
    public List<Message> getUnreadMessages(Long userId) {
        return messageMapper.findUnreadByUserId(userId);
    }

    /**
     * 获取未读消息数量
     */
    public int getUnreadCount(Long userId) {
        return messageMapper.countUnread(userId);
    }

    /**
     * 标记单条消息为已读
     */
    public void markAsRead(Long messageId) {
        messageMapper.markAsRead(messageId);
    }

    /**
     * 标记所有消息为已读
     */
    public void markAllAsRead(Long userId) {
        messageMapper.markAllAsRead(userId);
    }
}
