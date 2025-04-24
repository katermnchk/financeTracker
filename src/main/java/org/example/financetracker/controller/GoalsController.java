package org.example.financetracker.controller;

import org.example.financetracker.entity.Category;
import org.example.financetracker.entity.User;
import org.example.financetracker.security.CustomUserDetails;
import org.example.financetracker.service.CategoryService;
import org.example.financetracker.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class GoalsController {

    private final GoalService goalService;
    private final CategoryService categoryService;

    @Autowired
    public GoalsController(GoalService goalService, CategoryService categoryService) {
        this.goalService = goalService;
        this.categoryService = categoryService;
    }

    @GetMapping("/goals")
    public String goals(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login?error";
        }

        User user = userDetails.getUser();
        List<Category> categories = categoryService.getAvailableCategories(user);
        model.addAttribute("goals", goalService.getGoalsForMonth(user.getId(), LocalDate.now().withDayOfMonth(1)));
        model.addAttribute("categories", categories);
        model.addAttribute("month", LocalDate.now().withDayOfMonth(1));
        return "goals";
    }

    @PostMapping("/goals/add")
    public String addGoal(@AuthenticationPrincipal CustomUserDetails userDetails,
                          @RequestParam("categoryId") Long categoryId,
                          @RequestParam("amount") BigDecimal amount,
                          @RequestParam("month") String month,
                          @RequestParam("type") String type) {
        if (userDetails == null) {
            return "redirect:/login?error";
        }

        User user = userDetails.getUser();
        LocalDate goalMonth;
        try {
            goalMonth = LocalDate.parse(month + "-01");
        } catch (Exception e) {
            System.out.println("Ошибка парсинга месяца: " + month + ", использую текущий месяц");
            goalMonth = LocalDate.now().withDayOfMonth(1);
        }

        goalService.createGoal(user, categoryId, amount, goalMonth, type);
        return "redirect:/goals";
    }

    @PostMapping("/goals/delete/{id}")
    public String deleteGoal(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login?error";
        }

        User user = userDetails.getUser();
        goalService.deleteGoal(id, user);
        return "redirect:/goals";
    }
}