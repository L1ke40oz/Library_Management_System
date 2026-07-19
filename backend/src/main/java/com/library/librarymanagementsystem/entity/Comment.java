package com.library.librarymanagementsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long userId;
    private Long bookId;
    private String content;
    private LocalDateTime createdAt;

    // 联查时使用
    private String username;
}
