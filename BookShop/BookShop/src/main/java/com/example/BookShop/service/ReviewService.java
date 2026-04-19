package com.example.BookShop.service;

import com.example.BookShop.model.*;
import com.example.BookShop.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;

    public ReviewService(ReviewRepository reviewRepo,
                         BookRepository bookRepo,
                         UserRepository userRepo) {
        this.reviewRepo = reviewRepo;
        this.bookRepo   = bookRepo;
        this.userRepo   = userRepo;
    }

    // Lấy đánh giá đã duyệt (hiển thị trên trang sách)
    public List<Review> getApprovedReviews(Long bookId) {
        return reviewRepo.findByBookIdAndStatusOrderByCreatedAtDesc(bookId, ReviewStatus.APPROVED);
    }

    // Điểm trung bình
    public double getAvgRating(Long bookId) {
        return reviewRepo.avgRatingByBookId(bookId);
    }

    // Số đánh giá đã duyệt
    public long getReviewCount(Long bookId) {
        return reviewRepo.countByBookIdAndStatus(bookId, ReviewStatus.APPROVED);
    }

    // User đã review chưa?
    public boolean hasReviewed(Long bookId, Long userId) {
        return reviewRepo.existsByBookIdAndUserId(bookId, userId);
    }

    // Gửi đánh giá mới
    @Transactional
    public void submitReview(Long bookId, Long userId, int rating, String comment) {
        if (reviewRepo.existsByBookIdAndUserId(bookId, userId)) {
            throw new RuntimeException("Bạn đã đánh giá cuốn sách này rồi!");
        }
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Điểm đánh giá phải từ 1 đến 5 sao!");
        }

        Book book = bookRepo.findById(bookId).orElseThrow();
        User user = userRepo.findById(userId).orElseThrow();

        Review review = new Review();
        review.setBook(book);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : "");
        review.setCreatedAt(LocalDateTime.now());
        review.setStatus(ReviewStatus.PENDING);
        reviewRepo.save(review);
    }

    // ===== ADMIN =====

    // Tất cả đánh giá
    public List<Review> getAllReviews() {
        return reviewRepo.findAllByOrderByCreatedAtDesc();
    }

    // Đánh giá chờ duyệt
    public List<Review> getPendingReviews() {
        return reviewRepo.findByStatusOrderByCreatedAtDesc(ReviewStatus.PENDING);
    }

    // Duyệt đánh giá
    @Transactional
    public void approveReview(Long reviewId) {
        Review review = reviewRepo.findById(reviewId).orElseThrow();
        review.setStatus(ReviewStatus.APPROVED);
        reviewRepo.save(review);
    }

    // Từ chối / ẩn đánh giá
    @Transactional
    public void rejectReview(Long reviewId) {
        Review review = reviewRepo.findById(reviewId).orElseThrow();
        review.setStatus(ReviewStatus.REJECTED);
        reviewRepo.save(review);
    }

    // Xóa vĩnh viễn
    @Transactional
    public void deleteReview(Long reviewId) {
        reviewRepo.deleteById(reviewId);
    }
}