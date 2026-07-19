package com.library.librarymanagementsystem.mapper;

import com.library.librarymanagementsystem.entity.Admin;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AdminMapper {

    @Insert("INSERT INTO admin(admin_name, username, password, permission, user_id) " +
            "VALUES(#{adminName}, #{username}, #{password}, #{permission}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "adminId")
    int insert(Admin admin);

    @Select("SELECT * FROM admin WHERE user_id = #{userId}")
    Admin findByUserId(Long userId);

    @Delete("DELETE FROM admin WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);

    @Update("UPDATE admin SET admin_name = #{adminName}, username = #{username}, " +
            "password = #{password}, permission = #{permission} WHERE user_id = #{userId}")
    int updateByUserId(Admin admin);
}
