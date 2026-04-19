package com.example.BookShop.controller;

import com.example.BookShop.model.Coupon;
import com.example.BookShop.model.DiscountType;
import com.example.BookShop.repository.CouponRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/coupons")
public class AdminCouponController {

    private final CouponRepository couponRepo;

    public AdminCouponController(CouponRepository couponRepo) {
        this.couponRepo = couponRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("coupons", couponRepo.findAll());
        return "admin/coupons/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("coupon", new Coupon());
        model.addAttribute("discountTypes", DiscountType.values());
        return "admin/coupons/form";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Coupon coupon,
                      @RequestParam(required = false)
                      @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime expiredAt,
                      RedirectAttributes ra) {
        coupon.setExpiredAt(expiredAt);
        couponRepo.save(coupon);
        ra.addFlashAttribute("successMsg", "Đã thêm mã: " + coupon.getCode());
        return "redirect:/admin/coupons";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("coupon", couponRepo.findById(id).orElseThrow());
        model.addAttribute("discountTypes", DiscountType.values());
        return "admin/coupons/form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @ModelAttribute Coupon coupon,
                       @RequestParam(required = false)
                       @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime expiredAt,
                       RedirectAttributes ra) {
        coupon.setId(id);
        coupon.setExpiredAt(expiredAt);
        couponRepo.save(coupon);
        ra.addFlashAttribute("successMsg", "Đã cập nhật mã: " + coupon.getCode());
        return "redirect:/admin/coupons";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        Coupon coupon = couponRepo.findById(id).orElseThrow();
        coupon.setActive(!coupon.isActive());
        couponRepo.save(coupon);
        ra.addFlashAttribute("successMsg",
                coupon.isActive() ? "Đã kích hoạt mã." : "Đã vô hiệu hóa mã.");
        return "redirect:/admin/coupons";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        couponRepo.deleteById(id);
        ra.addFlashAttribute("successMsg", "Đã xóa mã giảm giá.");
        return "redirect:/admin/coupons";
    }
}