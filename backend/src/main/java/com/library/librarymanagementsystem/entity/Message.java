package com.library.librarymanagementsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private Long messageId;
    private Long userId;
    private String title;
    private String content;
    private String type;       // SYSTEM / OVERDUE / NOTICE
    private Integer isRead;    // 0未读 1已读
    private LocalDateTime createdAt;
}
