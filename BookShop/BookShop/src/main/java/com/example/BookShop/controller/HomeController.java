package com.example.BookShop.controller;

import com.example.BookShop.model.Book;
import com.example.BookShop.repository.BookRepository;
import com.example.BookShop.repository.CategoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final BookRepository bookRepo;
    private final CategoryRepository categoryRepo;

    public HomeController(BookRepository bookRepo, CategoryRepository categoryRepo) {
        this.bookRepo = bookRepo;
        this.categoryRepo = categoryRepo;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Book> newestBooks = bookRepo.findAll(
                PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "id"))
        ).getContent();

        List<Book> saleBooks = bookRepo.findBySalePriceGreaterThan(0,
                PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "id"))
        ).getContent();

        model.addAttribute("newestBooks", newestBooks);
        model.addAttribute("saleBooks", saleBooks);
        model.addAttribute("categories", categoryRepo.findAll());
        return "index";
    }
}