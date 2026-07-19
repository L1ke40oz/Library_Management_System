package com.library.librarymanagementsystem.mapper;

import com.library.librarymanagementsystem.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Insert("INSERT INTO comment(user_id, book_id, content) VALUES(#{userId}, #{bookId}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    @Select("SELECT c.*, u.username FROM comment c JOIN users u ON c.user_id = u.id WHERE c.book_id = #{bookId} ORDER BY c.created_at DESC")
    List<Comment> findByBookId(Long bookId);

    @Select("SELECT c.*, u.username FROM comment c JOIN users u ON c.user_id = u.id WHERE c.book_id = #{bookId} ORDER BY c.created_at DESC LIMIT #{limit}")
    List<Comment> findByBookIdLimit(@Param("bookId") Long bookId, @Param("limit") int limit);

    @Delete("DELETE FROM comment WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
}
