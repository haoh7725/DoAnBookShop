package com.example.BookShop.service;

import com.example.BookShop.model.*;
import com.example.BookShop.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;

    public CartService(CartItemRepository cartItemRepo,
                       BookRepository bookRepo,
                       UserRepository userRepo) {
        this.cartItemRepo = cartItemRepo;
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
    }

    public List<CartItem> getItems(Long userId) {
        return cartItemRepo.findByUserId(userId);
    }

    public int countItems(Long userId) {
        return cartItemRepo.countByUserId(userId);
    }

    public double getTotal(Long userId) {
        return cartItemRepo.findByUserId(userId).stream()
                .mapToDouble(item -> {
                    double price = item.getBook().getSalePrice() > 0
                            ? item.getBook().getSalePrice()
                            : item.getBook().getPrice();
                    return price * item.getQuantity();
                }).sum();
    }

    @Transactional
    public String addItem(Long userId, Long bookId) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));

        if (book.getStock() <= 0) {
            return "Sách đã hết hàng!";
        }

        cartItemRepo.findByUserIdAndBookId(userId, bookId).ifPresentOrElse(
                item -> {
                    int newQty = item.getQuantity() + 1;
                    if (newQty > book.getStock()) {
                        throw new RuntimeException("Vượt quá số lượng tồn kho!");
                    }
                    item.setQuantity(newQty);
                    cartItemRepo.save(item);
                },
                () -> {
                    User user = userRepo.findById(userId).orElseThrow();
                    CartItem item = new CartItem();
                    item.setUser(user);
                    item.setBook(book);
                    item.setQuantity(1);
                    cartItemRepo.save(item);
                }
        );
        return null; // null = thành công
    }

    @Transactional
    public void updateQuantity(Long userId, Long itemId, int quantity) {
        CartItem item = cartItemRepo.findById(itemId).orElseThrow();
        if (!item.getUser().getId().equals(userId)) return;

        if (quantity <= 0) {
            cartItemRepo.delete(item);
        } else if (quantity <= item.getBook().getStock()) {
            item.setQuantity(quantity);
            cartItemRepo.save(item);
        }
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        CartItem item = cartItemRepo.findById(itemId).orElseThrow();
        if (item.getUser().getId().equals(userId)) {
            cartItemRepo.delete(item);
        }
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepo.deleteByUserId(userId);
    }
}