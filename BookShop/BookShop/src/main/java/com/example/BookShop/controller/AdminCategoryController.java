package com.example.BookShop.controller;

import com.example.BookShop.model.Category;
import com.example.BookShop.repository.CategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryRepository categoryRepo;

    public AdminCategoryController(CategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryRepo.findAll());
        return "admin/categories/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/categories/form";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Category category) {
        categoryRepo.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryRepo.findById(id).orElseThrow());
        return "admin/categories/form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @ModelAttribute Category category) {
        category.setId(id);
        categoryRepo.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        categoryRepo.deleteById(id);
        return "redirect:/admin/categories";
    }
}