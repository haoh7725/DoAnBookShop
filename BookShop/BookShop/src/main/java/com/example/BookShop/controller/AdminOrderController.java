package com.example.BookShop.controller;

import com.example.BookShop.model.Order;
import com.example.BookShop.model.OrderStatus;
import com.example.BookShop.repository.OrderDetailRepository;
import com.example.BookShop.repository.OrderRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderRepository       orderRepo;
    private final OrderDetailRepository orderDetailRepo;

    public AdminOrderController(OrderRepository orderRepo,
                                OrderDetailRepository orderDetailRepo) {
        this.orderRepo       = orderRepo;
        this.orderDetailRepo = orderDetailRepo;
    }

    // Danh sách — hỗ trợ lọc theo ?status=
    @GetMapping
    public String list(@RequestParam(required = false) OrderStatus status,
                       Model model) {
        Sort sort = Sort.by(Sort.Direction.DESC, "orderDate");

        List<Order> orders = (status == null)
                ? orderRepo.findAll(sort)
                : orderRepo.findByStatus(status, sort);

        model.addAttribute("orders",        orders);
        model.addAttribute("statuses",      OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        return "admin/orders/list";
    }

    // Chi tiết đơn hàng
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Order order = orderRepo.findById(id).orElseThrow();
        model.addAttribute("order",    order);
        model.addAttribute("details",  orderDetailRepo.findByOrderId(id));
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders/detail";
    }

    // Cập nhật trạng thái
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam OrderStatus status,
                               RedirectAttributes ra) {
        Order order = orderRepo.findById(id).orElseThrow();

        if (!isValidTransition(order.getStatus(), status)) {
            ra.addFlashAttribute("errorMsg",
                    "Không thể chuyển từ \"" + label(order.getStatus()) +
                            "\" sang \"" + label(status) + "\"!");
            return "redirect:/admin/orders/" + id;
        }

        order.setStatus(status);
        orderRepo.save(order);
        ra.addFlashAttribute("successMsg", "Đã cập nhật: " + label(status));
        return "redirect:/admin/orders/" + id;
    }

    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case PENDING   -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.SHIPPING;
            case SHIPPING  -> to == OrderStatus.COMPLETED;
            default        -> false;
        };
    }

    private String label(OrderStatus s) {
        return switch (s) {
            case PENDING   -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case SHIPPING  -> "Đang giao";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
        };
    }
}