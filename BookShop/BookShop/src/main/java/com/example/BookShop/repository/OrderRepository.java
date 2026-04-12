package com.example.BookShop.repository;

import com.example.BookShop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId); // lấy đơn hàng theo user
}