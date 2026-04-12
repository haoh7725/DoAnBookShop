package com.example.BookShop.controller;

import com.example.BookShop.model.*;
import com.example.BookShop.repository.*;
import com.example.BookShop.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderDetailRepository orderDetailRepo;
    private final UserRepository userRepo;

    public OrderController(OrderService orderService,
                           OrderDetailRepository orderDetailRepo,
                           UserRepository userRepo) {
        this.orderService = orderService;
        this.orderDetailRepo = orderDetailRepo;
        this.userRepo = userRepo;
    }

    private Long getUserId(Authentication auth) {
        return userRepo.findByUsername(auth.getName()).orElseThrow().getId();
    }

    @GetMapping
    public String myOrders(Authentication auth, Model model) {
        Long userId = getUserId(auth);
        model.addAttribute("orders", orderService.getUserOrders(userId));
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Authentication auth, Model model) {
        Long userId = getUserId(auth);
        Order order = orderService.getOrderById(id);
        if (!order.getUser().getId().equals(userId)) return "redirect:/orders";

        model.addAttribute("order", order);
        model.addAttribute("details", orderDetailRepo.findByOrderId(id));
        return "orders/detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id, getUserId(auth));
            redirectAttributes.addFlashAttribute("successMsg", "Đã hủy đơn hàng thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}