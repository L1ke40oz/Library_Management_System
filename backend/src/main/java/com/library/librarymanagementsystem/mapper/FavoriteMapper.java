package com.library.librarymanagementsystem.mapper;

import com.library.librarymanagementsystem.entity.Book;
import com.library.librarymanagementsystem.entity.Favorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    @Insert("INSERT INTO favorite(user_id, book_id) VALUES(#{userId}, #{bookId})")
    int insert(Favorite favorite);

    @Delete("DELETE FROM favorite WHERE user_id = #{userId} AND book_id = #{bookId}")
    int delete(@Param("userId") Long userId, @Param("bookId") Long bookId);

    @Select("SELECT * FROM favorite WHERE user_id = #{userId} AND book_id = #{bookId}")
    Favorite findByUserAndBook(@Param("userId") Long userId, @Param("bookId") Long bookId);

    @Delete("DELETE FROM favorite WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);

    @Select("SELECT b.* FROM favorite f JOIN book b ON f.book_id = b.book_id WHERE f.user_id = #{userId} ORDER BY f.created_at DESC")
    List<Book> findBooksByUserId(Long userId);
}
