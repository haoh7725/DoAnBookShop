package com.example.BookShop.controller;

import com.example.BookShop.model.User;
import com.example.BookShop.repository.UserRepository;
import com.example.BookShop.service.CartService;
import com.example.BookShop.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserRepository userRepo;

    public CheckoutController(CartService cartService,
                              OrderService orderService,
                              UserRepository userRepo) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userRepo = userRepo;
    }

    private Long getUserId(Authentication auth) {
        return userRepo.findByUsername(auth.getName()).orElseThrow().getId();
    }

    @GetMapping
    public String checkoutPage(Authentication auth, Model model) {
        Long userId = getUserId(auth);
        var items = cartService.getItems(userId);
        if (items.isEmpty()) return "redirect:/cart";

        User user = userRepo.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("cartItems", items);
        model.addAttribute("total", cartService.getTotal(userId));
        model.addAttribute("user", user);
        return "checkout/index";
    }

    @PostMapping
    public String placeOrder(@RequestParam String address,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        try {
            Long userId = getUserId(auth);
            var order = orderService.placeOrder(userId, address);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Đặt hàng thành công! Mã đơn: #" + order.getId());
            return "redirect:/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/checkout";
        }
    }
}