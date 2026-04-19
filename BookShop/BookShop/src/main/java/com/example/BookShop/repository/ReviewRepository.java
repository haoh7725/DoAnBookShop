package com.example.BookShop.repository;

import com.example.BookShop.model.Review;
import com.example.BookShop.model.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Lấy đánh giá đã duyệt của 1 sách (hiển thị công khai)
    List<Review> findByBookIdAndStatusOrderByCreatedAtDesc(Long bookId, ReviewStatus status);

    // Tất cả đánh giá của 1 sách (admin)
    List<Review> findByBookIdOrderByCreatedAtDesc(Long bookId);

    // Tất cả đánh giá theo trạng thái (admin)
    List<Review> findByStatusOrderByCreatedAtDesc(ReviewStatus status);

    // Tất cả đánh giá (admin quản lý)
    List<Review> findAllByOrderByCreatedAtDesc();

    // Kiểm tra user đã review cuốn sách này chưa
    boolean existsByBookIdAndUserId(Long bookId, Long userId);

    // Điểm trung bình của sách
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.book.id = :bookId AND r.status = 'APPROVED'")
    double avgRatingByBookId(@Param("bookId") Long bookId);

    // Đếm số review đã duyệt
    long countByBookIdAndStatus(Long bookId, ReviewStatus status);
}