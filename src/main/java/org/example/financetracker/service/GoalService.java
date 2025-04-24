package org.example.financetracker.service;

import org.example.financetracker.entity.Category;
import org.example.financetracker.entity.Goal;
import org.example.financetracker.entity.Transaction;
import org.example.financetracker.entity.User;
import org.example.financetracker.repository.CategoryRepository;
import org.example.financetracker.repository.GoalRepository;
import org.example.financetracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public GoalService(GoalRepository goalRepository, TransactionRepository transactionRepository,
                       CategoryRepository categoryRepository) {
        this.goalRepository = goalRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Goal> getGoalsForMonth(Long userId, LocalDate month) {
        List<Goal> goals = goalRepository.findByUserIdAndMonthLessThanEqualAndMonthGreaterThanEqual(
                userId, month.withDayOfMonth(month.lengthOfMonth()), month.withDayOfMonth(1));
        System.out.println("Цели для userId=" + userId + ", month=" + month + ": " + goals);
        if (goals.isEmpty()) {
            System.out.println("Предупреждение: Цели не найдены для userId=" + userId + ", month=" + month);
        }
        return goals;
    }

    public List<Goal> getGoalsForYear(Long userId, int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        List<Goal> goals = goalRepository.findByUserIdAndMonthBetweenOrderByMonthAsc(userId, start, end);
        System.out.println("Цели за год " + year + " для userId=" + userId + ": " + goals);
        return goals;
    }

    public List<Goal> getGoalsSortedByAmount(Long userId) {
        List<Goal> goals = goalRepository.findByUserIdOrderByAmountDesc(userId);
        System.out.println("Цели, отсортированные по сумме, для userId=" + userId + ": " + goals);
        return goals;
    }

    public BigDecimal getSpentForGoal(Goal goal) {
        if (goal == null || goal.getUser() == null || goal.getCategory() == null || goal.getMonth() == null) {
            System.out.println("Ошибка: Некорректная цель для расчёта потраченного: " + goal);
            return BigDecimal.ZERO;
        }

        LocalDateTime startDate = goal.getMonth().atStartOfDay();
        LocalDateTime endDate = goal.getMonth().withDayOfMonth(goal.getMonth().lengthOfMonth())
                .atTime(23, 59, 59, 999999999);

        List<Transaction> transactions = transactionRepository.findByUserIdAndCategoryIdAndDateBetween(
                goal.getUser().getId(),
                goal.getCategory().getId(),
                startDate,
                endDate
        );

        BigDecimal spent = transactions.stream()
                .filter(t -> t != null && t.getType() != null && t.getType().equals(goal.getType()))
                .map(Transaction::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("Потрачено/заработано для цели id=" + goal.getId() + ": " + spent);
        return spent;
    }

    public double getProgressPercentage(Goal goal) {
        if (goal == null || goal.getAmount() == null || goal.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Ошибка: Некорректная цель для расчёта процента: " + goal);
            return 0.0;
        }

        BigDecimal spent = getSpentForGoal(goal);
        if (spent == null) {
            spent = BigDecimal.ZERO;
        }

        try {
            BigDecimal percentage = spent.divide(goal.getAmount(), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            return percentage.doubleValue();
        } catch (ArithmeticException e) {
            System.out.println("Ошибка деления при расчёте процента для цели id=" + goal.getId() + ": " + e.getMessage());
            return 0.0;
        }
    }

    public void createGoal(User user, Long categoryId, BigDecimal amount, LocalDate month, String type) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена с ID: " + categoryId));

        if (!category.getIsDefault() && (category.getUser() == null || !category.getUser().getId().equals(user.getId()))) {
            throw new SecurityException("Категория недоступна для этого пользователя");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма цели должна быть больше нуля");
        }

        if (!"INCOME".equals(type) && !"EXPENSE".equals(type)) {
            throw new IllegalArgumentException("Неверный тип цели: " + type);
        }

        if (month == null) {
            throw new IllegalArgumentException("Месяц цели не указан");
        }

        // проверка на дублирование цели
        if (goalRepository.existsByUserIdAndCategoryIdAndMonthAndType(user.getId(), categoryId, month, type)) {
            throw new IllegalArgumentException("Цель для категории " + category.getName() + ", месяца " + month + " и типа " + type + " уже существует");
        }

        Goal goal = new Goal();
        goal.setUser(user);
        goal.setCategory(category);
        goal.setAmount(amount);
        goal.setMonth(month);
        goal.setType(type);

        goalRepository.save(goal);
        System.out.println("Цель успешно создана: " + goal);
    }

    public void deleteGoal(Long id, User user) {
        Goal goal = goalRepository.findById(id)
                .filter(g -> g.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Цель не найдена или доступ запрещён"));
        goalRepository.delete(goal);
        System.out.println("Цель успешно удалена: " + id);
    }

    public List<Goal> getGoalsByType(Long userId, LocalDate month, String type) {
        List<Goal> goals = goalRepository.findByUserIdAndMonthAndType(userId, month, type);
        System.out.println("Цели для userId=" + userId + ", month=" + month + ", type=" + type + ": " + goals);
        return goals;
    }
}