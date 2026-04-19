package com.example.BookShop.controller;

import com.example.BookShop.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLogin() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegister() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(defaultValue = "") String fullName,
                           @RequestParam(defaultValue = "") String email,
                           @RequestParam(defaultValue = "") String phone,
                           Model model) {

        // --- Validation ---
        if (username.isBlank() || username.length() < 3) {
            model.addAttribute("error", "Tên đăng nhập phải có ít nhất 3 ký tự!");
            return "auth/register";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự!");
            return "auth/register";
        }
        if (!email.isBlank() && !email.matches("^[\\w.+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")) {
            model.addAttribute("error", "Email không đúng định dạng!");
            return "auth/register";
        }
        if (!phone.isBlank() && !phone.matches("^(0|\\+84)[0-9]{8,10}$")) {
            model.addAttribute("error", "Số điện thoại không hợp lệ! (VD: 0901234567)");
            return "auth/register";
        }
        if (userService.existsByUsername(username)) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại!");
            return "auth/register";
        }

        // Giữ lại giá trị đã nhập khi có lỗi
        model.addAttribute("prevUsername", username);
        model.addAttribute("prevFullName", fullName);
        model.addAttribute("prevEmail",    email);
        model.addAttribute("prevPhone",    phone);

        userService.register(username, password, fullName, email, phone);
        return "redirect:/login?registered=true";
    }
}