package com.example.BookShop.controller;

import com.example.BookShop.model.Book;
import com.example.BookShop.model.ReviewStatus;
import com.example.BookShop.model.User;
import com.example.BookShop.repository.BookRepository;
import com.example.BookShop.repository.CategoryRepository;
import com.example.BookShop.repository.UserRepository;
import com.example.BookShop.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookRepository bookRepo;
    private final CategoryRepository categoryRepo;
    private final ReviewService reviewService;
    private final UserRepository userRepo;

    public BookController(BookRepository bookRepo,
                          CategoryRepository categoryRepo,
                          ReviewService reviewService,
                          UserRepository userRepo) {
        this.bookRepo = bookRepo;
        this.categoryRepo = categoryRepo;
        this.reviewService = reviewService;
        this.userRepo = userRepo;
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
    public String bookDetail(@PathVariable Long id,
                             Authentication auth,
                             Model model) {
        Book book = bookRepo.findById(id).orElseThrow();
        model.addAttribute("book", book);

        // Thêm dữ liệu đánh giá
        model.addAttribute("reviews",     reviewService.getApprovedReviews(id));
        model.addAttribute("avgRating",   reviewService.getAvgRating(id));
        model.addAttribute("reviewCount", reviewService.getReviewCount(id));

        // Kiểm tra user đã review chưa
        boolean hasReviewed = false;
        if (auth != null && auth.isAuthenticated()) {
            User user = userRepo.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                hasReviewed = reviewService.hasReviewed(id, user.getId());
            }
        }
        model.addAttribute("hasReviewed", hasReviewed);

        return "books/detail";
    }

    // Quick View — fragment HTML
    @GetMapping("/{id}/quickview")
    public String quickView(@PathVariable Long id, Model model) {
        Book book = bookRepo.findById(id).orElseThrow();
        model.addAttribute("book", book);
        model.addAttribute("avgRating",   reviewService.getAvgRating(id));
        model.addAttribute("reviewCount", reviewService.getReviewCount(id));
        return "books/quickview";
    }
}