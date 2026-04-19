package com.example.BookShop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    // PERCENT hoặc FIXED
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private double discountValue;       // % hoặc số tiền cố định
    private double minOrderAmount;      // đơn tối thiểu để dùng mã
    private int usageLimit;             // số lần dùng tối đa (0 = không giới hạn)
    private int usedCount;              // đã dùng bao nhiêu lần
    private boolean active = true;

    private LocalDateTime expiredAt;    // null = không hết hạn
}