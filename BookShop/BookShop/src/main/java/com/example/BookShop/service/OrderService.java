package com.example.BookShop.service;

import com.example.BookShop.model.*;
import com.example.BookShop.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderDetailRepository orderDetailRepo;
    private final CartService cartService;
    private final CartItemRepository cartItemRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;

    public OrderService(OrderRepository orderRepo,
                        OrderDetailRepository orderDetailRepo,
                        CartService cartService,
                        CartItemRepository cartItemRepo,
                        BookRepository bookRepo,
                        UserRepository userRepo) {
        this.orderRepo = orderRepo;
        this.orderDetailRepo = orderDetailRepo;
        this.cartService = cartService;
        this.cartItemRepo = cartItemRepo;
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public Order placeOrder(Long userId, String address) {
        List<CartItem> items = cartItemRepo.findByUserId(userId);
        if (items.isEmpty()) throw new RuntimeException("Giỏ hàng trống!");

        User user = userRepo.findById(userId).orElseThrow();

        // Tạo đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        double total = 0;
        for (CartItem item : items) {
            Book book = item.getBook();

            // Kiểm tra tồn kho
            if (book.getStock() < item.getQuantity()) {
                throw new RuntimeException("Sách \"" + book.getTitle() + "\" không đủ hàng!");
            }

            double unitPrice = book.getSalePrice() > 0 ? book.getSalePrice() : book.getPrice();
            total += unitPrice * item.getQuantity();

            // Trừ tồn kho
            book.setStock(book.getStock() - item.getQuantity());
            bookRepo.save(book);
        }
        order.setTotalPrice(total);
        orderRepo.save(order);

        // Lưu chi tiết đơn
        for (CartItem item : items) {
            double unitPrice = item.getBook().getSalePrice() > 0
                    ? item.getBook().getSalePrice() : item.getBook().getPrice();
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setBook(item.getBook());
            detail.setQuantity(item.getQuantity());
            detail.setPrice(unitPrice);
            orderDetailRepo.save(detail);
        }

        // Xóa giỏ hàng
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