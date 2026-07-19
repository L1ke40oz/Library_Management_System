package com.library.librarymanagementsystem.controller;

import com.library.librarymanagementsystem.dto.ApiResponse;
import com.library.librarymanagementsystem.entity.Comment;
import com.library.librarymanagementsystem.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentMapper commentMapper;

    @PostMapping
    public ApiResponse<?> addComment(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long bookId = Long.valueOf(body.get("bookId").toString());
        String content = (String) body.get("content");

        if (content == null || content.trim().isEmpty()) {
            return ApiResponse.error(400, "评论内容不能为空");
        }
        if (content.length() > 200) {
            return ApiResponse.error(400, "评论内容不能超过200字");
        }

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setBookId(bookId);
        comment.setContent(content.trim());
        commentMapper.insert(comment);

        return ApiResponse.success("评论成功", null);
    }

    @GetMapping("/{bookId}")
    public ApiResponse<List<Comment>> getComments(@PathVariable Long bookId) {
        List<Comment> comments = commentMapper.findByBookId(bookId);
        return ApiResponse.success("查询成功", comments);
    }

    @GetMapping("/{bookId}/latest")
    public ApiResponse<List<Comment>> getLatestComments(@PathVariable Long bookId, @RequestParam(defaultValue = "5") int limit) {
        List<Comment> comments = commentMapper.findByBookIdLimit(bookId, limit);
        return ApiResponse.success("查询成功", comments);
    }
}
