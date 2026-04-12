package com.example.BookShop.service;

import com.example.BookShop.model.Role;
import com.example.BookShop.model.User;
import com.example.BookShop.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // Spring Security gọi hàm này khi đăng nhập
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(!user.isEnabled())
                .build();
    }

    // Đăng ký user mới
    public void register(String username, String password, String fullName,
                         String email, String phone) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(Role.USER);
        user.setEnabled(true);
        userRepo.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepo.findByUsername(username).isPresent();
    }
}