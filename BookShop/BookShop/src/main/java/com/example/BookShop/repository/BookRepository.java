package com.example.BookShop.repository;

import com.example.BookShop.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContaining(String keyword); // tìm kiếm theo tên
    List<Book> findByCategoryId(Long categoryId);     // lọc theo danh mục
}

