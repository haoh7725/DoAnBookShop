package com.example.BookShop.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String fullName;
    private String email;
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean enabled = true;
}
