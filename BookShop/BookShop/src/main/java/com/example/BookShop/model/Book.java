package com.example.BookShop.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String author;
    private String publisher;
    private int publishYear;
    private String description;

    @Column(nullable = false)
    private double price;

    private double salePrice;
    private int stock;
    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
