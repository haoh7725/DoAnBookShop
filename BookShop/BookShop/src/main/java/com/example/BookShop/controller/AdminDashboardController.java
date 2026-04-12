package com.example.BookShop.controller;

import com.example.BookShop.model.OrderStatus;
import com.example.BookShop.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final BookRepository bookRepo;

    public AdminDashboardController(OrderRepository orderRepo,
                                    UserRepository userRepo,
                                    BookRepository bookRepo) {
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
        this.bookRepo = bookRepo;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalRevenue",    orderRepo.sumCompletedRevenue());
        model.addAttribute("pendingOrders",   orderRepo.countByStatus(OrderStatus.PENDING));
        model.addAttribute("totalUsers",      userRepo.count());
        model.addAttribute("totalBooks",      bookRepo.count());
        model.addAttribute("recentOrders",    orderRepo.findTop10ByOrderByOrderDateDesc());
        model.addAttribute("lowStockBooks",   bookRepo.findByStockLessThan(5));
        return "admin/dashboard";
    }
}