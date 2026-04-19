package com.example.BookShop.controller;

import com.example.BookShop.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Danh sách tất cả đánh giá
    @GetMapping
    public String list(@RequestParam(required = false) String filter, Model model) {
        var reviews = "pending".equals(filter)
                ? reviewService.getPendingReviews()
                : reviewService.getAllReviews();

        model.addAttribute("reviews", reviews);
        model.addAttribute("filter", filter);
        model.addAttribute("pendingCount",
                reviewService.getPendingReviews().size());
        return "admin/reviews/list";
    }

    // Duyệt đánh giá
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        reviewService.approveReview(id);
        ra.addFlashAttribute("successMsg", "Đã duyệt đánh giá.");
        return "redirect:/admin/reviews";
    }

    // Ẩn/từ chối đánh giá
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes ra) {
        reviewService.rejectReview(id);
        ra.addFlashAttribute("successMsg", "Đã ẩn đánh giá.");
        return "redirect:/admin/reviews";
    }

    // Xóa đánh giá
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        reviewService.deleteReview(id);
        ra.addFlashAttribute("successMsg", "Đã xóa đánh giá.");
        return "redirect:/admin/reviews";
    }
}