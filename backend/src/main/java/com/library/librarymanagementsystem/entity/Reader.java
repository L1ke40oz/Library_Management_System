package com.library.librarymanagementsystem.entity;

import lombok.Data;

@Data
public class Reader {
    private Long readerId;
    private String readerName;
    private String gender;
    private String className;
    private String contact;
    private String status;
    private Long userId;
}
