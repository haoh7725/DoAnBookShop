package com.example.BookShop.controller;

import com.example.BookShop.model.Order;
import com.example.BookShop.model.OrderStatus;
import com.example.BookShop.repository.OrderDetailRepository;
import com.example.BookShop.repository.OrderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderRepository orderRepo;
    private final OrderDetailRepository orderDetailRepo;

    public AdminOrderController(OrderRepository orderRepo,
                                OrderDetailRepository orderDetailRepo) {
        this.orderRepo = orderRepo;
        this.orderDetailRepo = orderDetailRepo;
    }

    // Danh sách tất cả đơn hàng
    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", orderRepo.findAll(
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "orderDate")));
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders/list";
    }

    // Chi tiết đơn hàng
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Order order = orderRepo.findById(id).orElseThrow();
        model.addAttribute("order", order);
        model.addAttribute("details", orderDetailRepo.findByOrderId(id));
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders/detail";
    }

    // Cập nhật trạng thái đơn hàng
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam OrderStatus status,
                               RedirectAttributes redirectAttributes) {
        Order order = orderRepo.findById(id).orElseThrow();

        // Validate luồng trạng thái hợp lệ
        if (!isValidTransition(order.getStatus(), status)) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Không thể chuyển từ \"" + labelOf(order.getStatus()) +
                            "\" sang \"" + labelOf(status) + "\"!");
            return "redirect:/admin/orders/" + id;
        }

        order.setStatus(status);
        orderRepo.save(order);
        redirectAttributes.addFlashAttribute("successMsg",
                "Đã cập nhật trạng thái thành: " + labelOf(status));
        return "redirect:/admin/orders/" + id;
    }

    // Quy định luồng chuyển trạng thái hợp lệ
    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case PENDING   -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.SHIPPING;
            case SHIPPING  -> to == OrderStatus.COMPLETED;
            default        -> false; // COMPLETED, CANCELLED không chuyển được nữa
        };
    }

    private String labelOf(OrderStatus s) {
        return switch (s) {
            case PENDING   -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case SHIPPING  -> "Đang giao";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
        };
    }
}