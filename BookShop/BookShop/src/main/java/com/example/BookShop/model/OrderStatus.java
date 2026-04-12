package com.example.BookShop.model;

public enum OrderStatus {
    PENDING,    // Chờ xác nhận
    CONFIRMED,  // Đã xác nhận
    SHIPPING,   // Đang giao
    COMPLETED,  // Hoàn thành
    CANCELLED   // Đã hủy
}
