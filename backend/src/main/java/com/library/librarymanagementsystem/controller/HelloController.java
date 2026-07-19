package com.library.librarymanagementsystem.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api") // 给所有接口加一个 /api 前缀
public class HelloController {

    @GetMapping("/hello") // 接口路径是 /api/hello
    public Map<String, Object> sayHello() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "成功从后端获取信息！");
        result.put("data", "Hello from Spring Boot Backend!");
        return result;
    }
}