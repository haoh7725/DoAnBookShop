package com.example.BookShop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime orderDate;
    private double totalPrice;   // sau khi giảm
    private double discount;     // số tiền đã giảm
    private String couponCode;   // mã đã dùng (null nếu không)
    private String address;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}