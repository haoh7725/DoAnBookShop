package com.example.BookShop.controller;

import com.example.BookShop.model.Coupon;
import com.example.BookShop.model.User;
import com.example.BookShop.repository.UserRepository;
import com.example.BookShop.service.CartService;
import com.example.BookShop.service.CouponService;
import com.example.BookShop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService    cartService;
    private final OrderService   orderService;
    private final UserRepository userRepo;
    private final CouponService  couponService;

    public CheckoutController(CartService cartService,
                              OrderService orderService,
                              UserRepository userRepo,
                              CouponService couponService) {
        this.cartService   = cartService;
        this.orderService  = orderService;
        this.userRepo      = userRepo;
        this.couponService = couponService;
    }

    private Long getUserId(Authentication auth) {
        return userRepo.findByUsername(auth.getName()).orElseThrow().getId();
    }

    @GetMapping
    public String checkoutPage(Authentication auth,
                               @RequestParam(required = false) String couponCode,
                               @RequestParam(required = false) String couponError,
                               Model model) {
        Long userId = getUserId(auth);
        var items = cartService.getItems(userId);
        if (items.isEmpty()) return "redirect:/cart";

        User user   = userRepo.findByUsername(auth.getName()).orElseThrow();
        double total = cartService.getTotal(userId);

        double discount   = 0;
        Coupon coupon     = null;

        if (couponCode != null && !couponCode.isBlank()) {
            try {
                coupon   = couponService.validate(couponCode, total);
                discount = couponService.calcDiscount(coupon, total);
            } catch (Exception e) {
                model.addAttribute("couponError", e.getMessage());
            }
        }

        model.addAttribute("cartItems",   items);
        model.addAttribute("total",       total);
        model.addAttribute("discount",    discount);
        model.addAttribute("finalTotal",  total - discount);
        model.addAttribute("couponCode",  couponCode);
        model.addAttribute("coupon",      coupon);
        model.addAttribute("user",        user);
        return "checkout/index";
    }

    // AJAX: kiểm tra mã giảm giá
    @PostMapping("/apply-coupon")
    @ResponseBody
    public ResponseEntity<?> applyCoupon(@RequestParam String code,
                                         Authentication auth) {
        Long userId  = getUserId(auth);
        double total = cartService.getTotal(userId);
        try {
            Coupon coupon   = couponService.validate(code, total);
            double discount = couponService.calcDiscount(coupon, total);
            String label    = coupon.getDiscountType().name().equals("PERCENT")
                    ? (int) coupon.getDiscountValue() + "%"
                    : String.format("%,.0fđ", coupon.getDiscountValue());
            return ResponseEntity.ok(Map.of(
                    "success",    true,
                    "discount",   discount,
                    "finalTotal", total - discount,
                    "label",      label
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping
    public String placeOrder(@RequestParam String address,
                             @RequestParam(required = false) String couponCode,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        try {
            Long userId = getUserId(auth);
            var order = orderService.placeOrder(userId, address, couponCode);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Đặt hàng thành công! Mã đơn: #" + order.getId());
            return "redirect:/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/checkout";
        }
    }
}