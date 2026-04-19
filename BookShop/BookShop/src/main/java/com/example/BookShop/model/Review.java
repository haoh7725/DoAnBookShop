package com.example.BookShop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int rating;          // 1-5 sao

    @Column(columnDefinition = "TEXT")
    private String comment;

    private LocalDateTime createdAt;

    // PENDING=chờ duyệt, APPROVED=đã duyệt, REJECTED=đã xóa/ẩn
    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.PENDING;
}