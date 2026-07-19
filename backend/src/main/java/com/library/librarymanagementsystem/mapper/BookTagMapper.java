package com.library.librarymanagementsystem.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookTagMapper {

    @Insert("INSERT INTO book_tag(book_id, tag) VALUES(#{bookId}, #{tag})")
    int insertTag(@Param("bookId") Long bookId, @Param("tag") String tag);

    @Delete("DELETE FROM book_tag WHERE book_id = #{bookId}")
    int deleteByBookId(Long bookId);

    @Select("SELECT tag FROM book_tag WHERE book_id = #{bookId}")
    List<String> findTagsByBookId(Long bookId);
}
