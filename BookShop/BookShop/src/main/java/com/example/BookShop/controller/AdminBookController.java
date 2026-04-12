package com.example.BookShop.controller;

import com.example.BookShop.model.Book;
import com.example.BookShop.repository.BookRepository;
import com.example.BookShop.repository.CategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;

@Controller
@RequestMapping("/admin/books")
public class AdminBookController {

    private final BookRepository bookRepo;
    private final CategoryRepository categoryRepo;

    public AdminBookController(BookRepository bookRepo, CategoryRepository categoryRepo) {
        this.bookRepo = bookRepo;
        this.categoryRepo = categoryRepo;
    }

    // Danh sách sách (admin)
    @GetMapping
    public String list(Model model) {
        model.addAttribute("books", bookRepo.findAll());
        return "admin/books/list";
    }

    // Hiện form thêm
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryRepo.findAll());
        return "admin/books/form";
    }

    // Lưu sách mới
    @PostMapping("/add")
    public String addBook(@ModelAttribute Book book,
                          @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        saveImage(book, imageFile);
        bookRepo.save(book);
        return "redirect:/admin/books";
    }

    // Hiện form sửa
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Book book = bookRepo.findById(id).orElseThrow();
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryRepo.findAll());
        return "admin/books/form";
    }

    // Cập nhật sách
    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable Long id,
                             @ModelAttribute Book book,
                             @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        book.setId(id);
        if (!imageFile.isEmpty()) {
            saveImage(book, imageFile);
        } else {
            // Giữ ảnh cũ
            Book existing = bookRepo.findById(id).orElseThrow();
            book.setImagePath(existing.getImagePath());
        }
        bookRepo.save(book);
        return "redirect:/admin/books";
    }

    // Xóa sách
    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookRepo.deleteById(id);
        return "redirect:/admin/books";
    }

    // Lưu ảnh vào thư mục uploads/
    private void saveImage(Book book, MultipartFile imageFile) throws IOException {
        if (!imageFile.isEmpty()) {
            String uploadDir = "uploads/books/";
            Files.createDirectories(Paths.get(uploadDir));
            String filename = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path path = Paths.get(uploadDir + filename);
            Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            book.setImagePath("/uploads/books/" + filename);
        }
    }
}