package com.example.BookShop.service;

import com.example.BookShop.model.*;
import com.example.BookShop.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository       orderRepo;
    private final OrderDetailRepository orderDetailRepo;
    private final CartService           cartService;
    private final CartItemRepository    cartItemRepo;
    private final BookRepository        bookRepo;
    private final UserRepository        userRepo;
    private final CouponService         couponService;

    public OrderService(OrderRepository orderRepo,
                        OrderDetailRepository orderDetailRepo,
                        CartService cartService,
                        CartItemRepository cartItemRepo,
                        BookRepository bookRepo,
                        UserRepository userRepo,
                        CouponService couponService) {
        this.orderRepo       = orderRepo;
        this.orderDetailRepo = orderDetailRepo;
        this.cartService     = cartService;
        this.cartItemRepo    = cartItemRepo;
        this.bookRepo        = bookRepo;
        this.userRepo        = userRepo;
        this.couponService   = couponService;
    }

    @Transactional
    public Order placeOrder(Long userId, String address, String couponCode) {
        List<CartItem> items = cartItemRepo.findByUserId(userId);
        if (items.isEmpty()) throw new RuntimeException("Giỏ hàng trống!");

        User user = userRepo.findById(userId).orElseThrow();

        // Tính tổng tiền gốc
        double subtotal = 0;
        for (CartItem item : items) {
            Book book = item.getBook();
            if (book.getStock() < item.getQuantity()) {
                throw new RuntimeException("Sách \"" + book.getTitle() + "\" không đủ hàng!");
            }
            double unitPrice = book.getSalePrice() > 0 ? book.getSalePrice() : book.getPrice();
            subtotal += unitPrice * item.getQuantity();
        }

        // Áp dụng coupon nếu có
        double discount = 0;
        Coupon coupon   = null;
        if (couponCode != null && !couponCode.isBlank()) {
            coupon   = couponService.validate(couponCode, subtotal);
            discount = couponService.calcDiscount(coupon, subtotal);
        }

        double totalPrice = subtotal - discount;

        // Tạo đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(totalPrice);
        order.setDiscount(discount);
        order.setCouponCode(couponCode);
        orderRepo.save(order);

        // Trừ tồn kho + lưu chi tiết
        for (CartItem item : items) {
            Book book      = item.getBook();
            double unitPrice = book.getSalePrice() > 0 ? book.getSalePrice() : book.getPrice();

            book.setStock(book.getStock() - item.getQuantity());
            bookRepo.save(book);

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setBook(book);
            detail.setQuantity(item.getQuantity());
            detail.setPrice(unitPrice);
            orderDetailRepo.save(detail);
        }

        // Đánh dấu coupon đã dùng
        if (coupon != null) {
            couponService.markUsed(coupon);
        }

        cartService.clearCart(userId);
        return order;
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepo.findByUserId(userId);
    }

    public Order getOrderById(Long orderId) {
        return orderRepo.findById(orderId).orElseThrow();
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepo.findById(orderId).orElseThrow();
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền hủy đơn này!");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy đơn khi đang ở trạng thái Chờ xác nhận!");
        }
        // Hoàn lại tồn kho
        List<OrderDetail> details = orderDetailRepo.findByOrderId(orderId);
        for (OrderDetail d : details) {
            Book book = d.getBook();
            book.setStock(book.getStock() + d.getQuantity());
            bookRepo.save(book);
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);
    }
}