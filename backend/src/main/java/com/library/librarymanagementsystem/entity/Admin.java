package com.library.librarymanagementsystem.entity;

import lombok.Data;

@Data
public class Admin {
    private Long adminId;
    private String adminName;
    private String username;
    private String password;
    private String permission;
    private Long userId;
}
