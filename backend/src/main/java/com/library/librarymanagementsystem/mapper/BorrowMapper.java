package com.library.librarymanagementsystem.mapper;

import com.library.librarymanagementsystem.entity.Borrow;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BorrowMapper {

    @Insert("INSERT INTO borrow(user_id, book_id, borrow_date, due_date, status) VALUES(#{userId}, #{bookId}, #{borrowDate}, #{dueDate}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "borrowId")
    int insert(Borrow borrow);

    @Update("UPDATE borrow SET status = #{status}, return_date = #{returnDate} WHERE borrow_id = #{borrowId}")
    int updateStatus(Borrow borrow);

    @Update("UPDATE borrow SET due_date = #{dueDate} WHERE borrow_id = #{borrowId}")
    int updateDueDate(@Param("borrowId") Long borrowId, @Param("dueDate") LocalDate dueDate);

    @Update("UPDATE borrow SET borrow_date = #{borrowDate} WHERE borrow_id = #{borrowId}")
    int updateBorrowDate(@Param("borrowId") Long borrowId, @Param("borrowDate") LocalDate borrowDate);

    @Select("SELECT br.*, b.book_name, b.author FROM borrow br JOIN book b ON br.book_id = b.book_id WHERE br.user_id = #{userId} ORDER BY br.borrow_date DESC")
    List<Borrow> findByUserId(Long userId);

    @Select("SELECT * FROM borrow WHERE user_id = #{userId} AND book_id = #{bookId} AND status = 'BORROWED'")
    Borrow findActiveBorrow(@Param("userId") Long userId, @Param("bookId") Long bookId);

    @Select("SELECT * FROM borrow WHERE borrow_id = #{borrowId}")
    Borrow findById(Long borrowId);

    // ===== 管理员查询 =====

    @Select("SELECT br.*, b.book_name, b.author, u.username FROM borrow br " +
            "JOIN book b ON br.book_id = b.book_id " +
            "JOIN users u ON br.user_id = u.id " +
            "ORDER BY br.borrow_date DESC LIMIT #{offset}, #{size}")
    List<Borrow> findAllPaged(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM borrow")
    int countAll();

    @Select("SELECT br.*, b.book_name, b.author, u.username FROM borrow br " +
            "JOIN book b ON br.book_id = b.book_id " +
            "JOIN users u ON br.user_id = u.id " +
            "WHERE br.status = #{status} " +
            "ORDER BY br.borrow_date DESC LIMIT #{offset}, #{size}")
    List<Borrow> findByStatusPaged(@Param("status") String status, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM borrow WHERE status = #{status}")
    int countByStatus(String status);

    @Select("SELECT br.*, b.book_name, b.author, u.username FROM borrow br " +
            "JOIN book b ON br.book_id = b.book_id " +
            "JOIN users u ON br.user_id = u.id " +
            "WHERE br.status = 'BORROWED' AND br.due_date < #{today} " +
            "ORDER BY br.due_date ASC")
    List<Borrow> findOverdue(@Param("today") LocalDate today);

    @Select("SELECT br.*, b.book_name, b.author, u.username FROM borrow br " +
            "JOIN book b ON br.book_id = b.book_id " +
            "JOIN users u ON br.user_id = u.id " +
            "WHERE u.username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR b.book_name LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY br.borrow_date DESC LIMIT #{offset}, #{size}")
    List<Borrow> searchPaged(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM borrow br " +
            "JOIN book b ON br.book_id = b.book_id " +
            "JOIN users u ON br.user_id = u.id " +
            "WHERE u.username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR b.book_name LIKE CONCAT('%', #{keyword}, '%')")
    int countByKeyword(String keyword);

    @Update("UPDATE borrow SET status = 'OVERDUE' WHERE status = 'BORROWED' AND due_date < #{today}")
    int markOverdue(@Param("today") LocalDate today);

    @Delete("DELETE FROM borrow WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
}
