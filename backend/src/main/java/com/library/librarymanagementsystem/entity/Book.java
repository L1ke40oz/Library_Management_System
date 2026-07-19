package com.library.librarymanagementsystem.entity;

import lombok.Data;

@Data
public class Book {
    private Long bookId;
    private String bookName;
    private String author;
    private String publisher;
    private String isbn;
    private String category;
    private String description;
    private Integer totalCount;
    private Integer borrowCount;
}
