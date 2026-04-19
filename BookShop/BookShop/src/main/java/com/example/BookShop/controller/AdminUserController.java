package com.example.BookShop.controller;

import com.example.BookShop.model.User;
import com.example.BookShop.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepo;

    public AdminUserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // Danh sách người dùng
    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userRepo.findAll());
        return "admin/users/list";
    }

    // Khóa / Mở khóa tài khoản
    @PostMapping("/{id}/toggle")
    public String toggleEnabled(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        User user = userRepo.findById(id).orElseThrow();

        // Không cho khóa chính ADMIN đang đăng nhập (an toàn cơ bản)
        if (user.getRole().name().equals("ADMIN")) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Không thể khóa tài khoản Admin!");
            return "redirect:/admin/users";
        }

        user.setEnabled(!user.isEnabled());
        userRepo.save(user);

        String msg = user.isEnabled() ? "Đã mở khóa tài khoản: " : "Đã khóa tài khoản: ";
        redirectAttributes.addFlashAttribute("successMsg", msg + user.getUsername());
        return "redirect:/admin/users";
    }
}