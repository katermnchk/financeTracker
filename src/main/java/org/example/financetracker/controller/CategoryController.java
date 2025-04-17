package org.example.financetracker.controller;

import org.example.financetracker.entity.Category;
import org.example.financetracker.entity.User;
import org.example.financetracker.service.CategoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model, @AuthenticationPrincipal User user) {
        if (user == null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("categories", categoryService.getUserCategories(user));
        model.addAttribute("category", new Category());
        return "categories";
    }

    @PostMapping
    public String createCategory(@ModelAttribute("category") Category category, @AuthenticationPrincipal User user) {
        if (user == null) {
            return "redirect:/dashboard";
        }
        categoryService.createCategory(category.getName(), category.getDescription(), user.getUsername());
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        if (user == null) {
            return "redirect:/login";
        }
        Category category = categoryService.getUserCategories(user).stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));
        model.addAttribute("category", category);
        model.addAttribute("categories", categoryService.getUserCategories(user));
        return "categories";
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute("category") Category category,
                                 @AuthenticationPrincipal User user) {
        if (user == null) {
            return "redirect:/login";
        }
        categoryService.updateCategory(id, category.getName(), category.getDescription(), user.getUsername());
        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user == null) {
            return "redirect:/login";
        }
        categoryService.deleteCategory(id, user.getUsername());
        return "redirect:/categories";
    }
}