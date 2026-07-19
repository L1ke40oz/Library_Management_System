package com.library.librarymanagementsystem.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Borrow {
    private Long borrowId;
    private Long userId;
    private Long bookId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status;

    // 联查时使用
    private String bookName;
    private String author;
    private String username;
}
