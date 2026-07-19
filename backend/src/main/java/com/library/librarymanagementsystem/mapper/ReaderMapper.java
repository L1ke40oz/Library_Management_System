package com.library.librarymanagementsystem.mapper;

import com.library.librarymanagementsystem.entity.Reader;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ReaderMapper {

    @Insert("INSERT INTO reader(reader_name, contact, status, user_id) " +
            "VALUES(#{readerName}, #{contact}, #{status}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "readerId")
    int insert(Reader reader);

    @Select("SELECT * FROM reader WHERE user_id = #{userId}")
    Reader findByUserId(Long userId);

    @Delete("DELETE FROM reader WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);

    @Update("UPDATE reader SET reader_name = #{readerName}, gender = #{gender}, " +
            "class_name = #{className}, contact = #{contact}, status = #{status} " +
            "WHERE user_id = #{userId}")
    int updateByUserId(Reader reader);
}
