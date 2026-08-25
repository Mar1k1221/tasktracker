package com.example.tasktracker.repository;

import com.example.tasktracker.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book,Integer> {

    List<Book> findByAuthor(String author);
    List<Book> findByYear(int year);
    @Query("SELECT b FROM Book b WHERE lower(b.title) LIKE lower(concat('%',:text,'%') ) ")
    Page<Book> findByTitleContainingIgnoreCase(@Param("text") String text, Pageable pageable);

}
