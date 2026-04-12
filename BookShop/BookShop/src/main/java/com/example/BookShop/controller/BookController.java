package com.example.BookShop.controller;

import com.example.BookShop.model.Book;
import com.example.BookShop.repository.BookRepository;
import com.example.BookShop.repository.CategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookRepository bookRepo;
    private final CategoryRepository categoryRepo;

    public BookController(BookRepository bookRepo, CategoryRepository categoryRepo) {
        this.bookRepo = bookRepo;
        this.categoryRepo = categoryRepo;
    }

    // Danh sách sách
    @GetMapping
    public String listBooks(Model model) {
        List<Book> books = bookRepo.findAll();
        model.addAttribute("books", books);
        model.addAttribute("categories", categoryRepo.findAll());
        return "books/list";
    }

    // Chi tiết sách
    @GetMapping("/{id}")
    public String bookDetail(@PathVariable Long id, Model model) {
        Book book = bookRepo.findById(id).orElseThrow();
        model.addAttribute("book", book);
        return "books/detail";
    }
}