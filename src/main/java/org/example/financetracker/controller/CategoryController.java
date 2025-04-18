package org.example.financetracker.controller;

import org.example.financetracker.entity.Category;
import org.example.financetracker.entity.User;
import org.example.financetracker.security.CustomUserDetails;
import org.example.financetracker.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model,@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            logger.warn("UserDetails = null");
            return "redirect:/login";
        }

        User user = userDetails.getUser();
        model.addAttribute("categories", categoryService.getUserCategories(user));
        model.addAttribute("category", new Category());
        return "categories";
    }

    @PostMapping
    public String createCategory(@ModelAttribute("category") Category category, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            logger.warn("UserDetails = null");
            return "redirect:/login";
        }

        User user = userDetails.getUser();
        categoryService.createCategory(category.getName(), category.getDescription(), user.getUsername());
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,  @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            logger.warn("UserDetails = null");
            return "redirect:/login";
        }

        User user = userDetails.getUser();
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
                                 @AuthenticationPrincipal CustomUserDetails userDetails ) {
        if (userDetails == null) {
            logger.warn("UserDetails = null");
            return "redirect:/login";
        }

        User user = userDetails.getUser();
        categoryService.updateCategory(id, category.getName(), category.getDescription(), user.getUsername());
        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            logger.warn("UserDetails = null");
            return "redirect:/login";
        }

        User user = userDetails.getUser();
        categoryService.deleteCategory(id, user.getUsername());
        return "redirect:/categories";
    }
}