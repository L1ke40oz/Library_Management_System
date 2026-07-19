package com.library.librarymanagementsystem.mapper;

import com.library.librarymanagementsystem.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);

    @Insert("INSERT INTO users(username, email, password, role) VALUES(#{username}, #{email}, #{password}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(User user);

    @Update("UPDATE users SET username = #{username}, email = #{email} WHERE id = #{id}")
    int updateUser(User user);

    // ===== 管理员操作 =====

    @Select("SELECT id, username, email, role, created_at FROM users ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<User> findAllPaged(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM users")
    int countAll();

    @Select("SELECT id, username, email, role, created_at FROM users " +
            "WHERE username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR email LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<User> searchPaged(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM users " +
            "WHERE username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR email LIKE CONCAT('%', #{keyword}, '%')")
    int countByKeyword(String keyword);

    @Select("SELECT id, username, email, role, created_at FROM users WHERE role = #{role} ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<User> findByRolePaged(@Param("role") String role, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM users WHERE role = #{role}")
    int countByRole(String role);

    @Update("UPDATE users SET role = #{role} WHERE id = #{id}")
    int updateRole(@Param("id") Long id, @Param("role") String role);

    @Update("UPDATE users SET username = #{username}, email = #{email}, role = #{role} WHERE id = #{id}")
    int updateUserFull(User user);

    @Update("UPDATE users SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteUser(Long id);
}
