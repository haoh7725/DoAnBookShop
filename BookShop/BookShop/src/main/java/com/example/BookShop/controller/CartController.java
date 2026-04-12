package com.example.BookShop.controller;

import com.example.BookShop.model.User;
import com.example.BookShop.repository.UserRepository;
import com.example.BookShop.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepo;

    public CartController(CartService cartService, UserRepository userRepo) {
        this.cartService = cartService;
        this.userRepo = userRepo;
    }

    private Long getUserId(Authentication auth) {
        User user = userRepo.findByUsername(auth.getName()).orElseThrow();
        return user.getId();
    }

    @GetMapping
    public String cartPage(Authentication auth, Model model) {
        Long userId = getUserId(auth);
        model.addAttribute("cartItems", cartService.getItems(userId));
        model.addAttribute("total", cartService.getTotal(userId));
        return "cart/index";
    }

    @PostMapping("/add/{bookId}")
    @ResponseBody
    public ResponseEntity<?> addToCart(@PathVariable Long bookId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.ok(Map.of("redirect", "/login"));
        }
        try {
            Long userId = getUserId(auth);
            String error = cartService.addItem(userId, bookId);
            if (error != null) {
                return ResponseEntity.ok(Map.of("success", false, "message", error));
            }
            int count = cartService.countItems(userId);
            return ResponseEntity.ok(Map.of("success", true, "cartCount", count));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/update/{itemId}")
    @ResponseBody
    public ResponseEntity<?> updateItem(@PathVariable Long itemId,
                                        @RequestParam int quantity,
                                        Authentication auth) {
        Long userId = getUserId(auth);
        cartService.updateQuantity(userId, itemId, quantity);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "total", cartService.getTotal(userId),
                "cartCount", cartService.countItems(userId)
        ));
    }

    @PostMapping("/remove/{itemId}")
    @ResponseBody
    public ResponseEntity<?> removeItem(@PathVariable Long itemId, Authentication auth) {
        Long userId = getUserId(auth);
        cartService.removeItem(userId, itemId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "total", cartService.getTotal(userId),
                "cartCount", cartService.countItems(userId)
        ));
    }

    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<?> cartCount(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.ok(Map.of("count", 0));
        }
        Long userId = getUserId(auth);
        return ResponseEntity.ok(Map.of("count", cartService.countItems(userId)));
    }
}