package com.example.BookShop.controller;

import com.example.BookShop.model.Book;
import com.example.BookShop.repository.BookRepository;
import com.example.BookShop.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookRepository bookRepo;
    private final CategoryRepository categoryRepo;

    public BookController(BookRepository bookRepo, CategoryRepository categoryRepo) {
        this.bookRepo = bookRepo;
        this.categoryRepo = categoryRepo;
    }

    @GetMapping
    public String listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Sort sortObj = switch (sort) {
            case "price_asc"  -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "title"      -> Sort.by("title").ascending();
            default           -> Sort.by(Sort.Direction.DESC, "id");
        };

        Page<Book> bookPage = bookRepo.searchBooks(
                (keyword != null && keyword.isBlank()) ? null : keyword,
                categoryId,
                minPrice,
                maxPrice,
                PageRequest.of(page, 24, sortObj)
        );

        model.addAttribute("books",       bookPage.getContent());
        model.addAttribute("totalPages",  bookPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("categories",  categoryRepo.findAll());
        model.addAttribute("keyword",     keyword);
        model.addAttribute("categoryId",  categoryId);
        model.addAttribute("minPrice",    minPrice);
        model.addAttribute("maxPrice",    maxPrice);
        model.addAttribute("sort",        sort);

        return "books/list";
    }

    @GetMapping("/{id}")
    public String bookDetail(@PathVariable Long id, Model model) {
        Book book = bookRepo.findById(id).orElseThrow();
        model.addAttribute("book", book);
        return "books/detail";
    }

    // Quick View — fragment HTML, không layout
    @GetMapping("/{id}/quickview")
    public String quickView(@PathVariable Long id, Model model) {
        Book book = bookRepo.findById(id).orElseThrow();
        model.addAttribute("book", book);
        return "books/quickview";
    }
}