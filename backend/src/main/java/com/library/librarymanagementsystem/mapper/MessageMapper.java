package com.library.librarymanagementsystem.mapper;

import com.library.librarymanagementsystem.entity.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MessageMapper {

    @Insert("INSERT INTO message(user_id, title, content, type, is_read) VALUES(#{userId}, #{title}, #{content}, #{type}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "messageId")
    int insert(Message message);

    @Select("SELECT * FROM message WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Message> findByUserId(Long userId);

    @Select("SELECT * FROM message WHERE user_id = #{userId} AND is_read = 0 ORDER BY created_at DESC")
    List<Message> findUnreadByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM message WHERE user_id = #{userId} AND is_read = 0")
    int countUnread(Long userId);

    @Update("UPDATE message SET is_read = 1 WHERE message_id = #{messageId}")
    int markAsRead(Long messageId);

    @Update("UPDATE message SET is_read = 1 WHERE user_id = #{userId}")
    int markAllAsRead(Long userId);

    @Delete("DELETE FROM message WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
}
