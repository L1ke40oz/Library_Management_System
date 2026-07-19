package com.library.librarymanagementsystem.mapper;

import com.library.librarymanagementsystem.entity.Book;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookMapper {

    @Select("SELECT * FROM book WHERE book_id = #{bookId}")
    Book findById(Long bookId);

    @Select("SELECT * FROM book WHERE book_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR author LIKE CONCAT('%', #{keyword}, '%') " +
            "OR isbn LIKE CONCAT('%', #{keyword}, '%')")
    List<Book> searchByKeyword(String keyword);

    @Select("SELECT * FROM book WHERE category = #{category}")
    List<Book> findByCategory(String category);

    @Select("SELECT * FROM book")
    List<Book> findAll();

    @Select("SELECT COUNT(*) FROM book")
    int countAll();

    @Select("SELECT * FROM book LIMIT #{offset}, #{size}")
    List<Book> findPage(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM book WHERE book_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR author LIKE CONCAT('%', #{keyword}, '%') " +
            "OR isbn LIKE CONCAT('%', #{keyword}, '%')")
    int countByKeyword(String keyword);

    @Select("SELECT * FROM book WHERE book_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR author LIKE CONCAT('%', #{keyword}, '%') " +
            "OR isbn LIKE CONCAT('%', #{keyword}, '%') LIMIT #{offset}, #{size}")
    List<Book> searchByKeywordPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT * FROM book ORDER BY RAND() LIMIT #{count}")
    List<Book> findRandom(@Param("count") int count);

    @Select("SELECT DISTINCT category FROM book")
    List<String> findAllCategories();

    @Select("SELECT b.* FROM book b JOIN book_tag t ON b.book_id = t.book_id WHERE t.tag = #{tag}")
    List<Book> findByTag(String tag);

    @Update("UPDATE book SET borrow_count = borrow_count + 1 WHERE book_id = #{bookId}")
    int incrementBorrowCount(Long bookId);

    @Update("UPDATE book SET borrow_count = borrow_count - 1 WHERE book_id = #{bookId} AND borrow_count > 0")
    int decrementBorrowCount(Long bookId);

    // ===== 管理员操作 =====

    @Insert("INSERT INTO book(book_name, author, publisher, isbn, category, description, total_count, borrow_count) " +
            "VALUES(#{bookName}, #{author}, #{publisher}, #{isbn}, #{category}, #{description}, #{totalCount}, #{borrowCount})")
    @Options(useGeneratedKeys = true, keyProperty = "bookId")
    int insertBook(Book book);

    @Update("UPDATE book SET book_name = #{bookName}, author = #{author}, publisher = #{publisher}, " +
            "isbn = #{isbn}, category = #{category}, description = #{description}, total_count = #{totalCount} " +
            "WHERE book_id = #{bookId}")
    int updateBook(Book book);

    @Delete("DELETE FROM book WHERE book_id = #{bookId}")
    int deleteBook(Long bookId);

    @Select("SELECT * FROM book WHERE isbn = #{isbn}")
    Book findByIsbn(String isbn);
}
