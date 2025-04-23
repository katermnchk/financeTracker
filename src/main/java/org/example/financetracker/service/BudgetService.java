package org.example.financetracker.service;

import org.example.financetracker.dto.BudgetDTO;
import org.example.financetracker.entity.Budget;
import org.example.financetracker.entity.Category;
import org.example.financetracker.entity.Transaction;
import org.example.financetracker.entity.User;
import org.example.financetracker.repository.BudgetRepository;
import org.example.financetracker.repository.CategoryRepository;
import org.example.financetracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         CategoryRepository categoryRepository,
                         TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void createBudget(BudgetDTO budgetDTO, User user) {
        Objects.requireNonNull(budgetDTO, "BudgetDTO не может быть null");
        Objects.requireNonNull(user, "Пользователь не может быть null");
        Objects.requireNonNull(budgetDTO.getCategoryId(), "ID категории не может быть null");
        Objects.requireNonNull(budgetDTO.getLimit(), "Лимит не может быть null");
        Objects.requireNonNull(budgetDTO.getMonth(), "Месяц не может быть null");

        if (budgetDTO.getLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Лимит должен быть больше нуля");
        }

        Category category = categoryRepository.findById(budgetDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена с ID: " + budgetDTO.getCategoryId()));
        if (!category.getIsDefault() && (category.getUser() == null || !category.getUser().getId().equals(user.getId()))) {
            throw new SecurityException("Категория недоступна для пользователя");
        }

        List<Budget> existingBudgets = budgetRepository.findByUserIdAndMonth(user.getId(), budgetDTO.getMonth());
        if (existingBudgets.stream().anyMatch(b -> b.getCategory().getId().equals(budgetDTO.getCategoryId()))) {
            throw new IllegalStateException("Бюджет для категории '" + category.getName() + "' на указанный месяц уже существует");
        }

        Budget budget = new Budget();
        budget.setCategory(category);
        budget.setUser(user);
        budget.setLimit(budgetDTO.getLimit());
        budget.setMonth(budgetDTO.getMonth());
        budgetRepository.save(budget);
        System.out.println("Бюджет успешно создан: " + budget);
    }

    public List<Budget> getBudgetsForMonth(Long userId, LocalDate month) {
        Objects.requireNonNull(userId, "ID пользователя не может быть null");
        Objects.requireNonNull(month, "Месяц не может быть null");
        List<Budget> budgets = budgetRepository.findByUserIdAndMonth(userId, month);
        System.out.println("Бюджеты для userId=" + userId + ", month=" + month + ": " + budgets);
        budgets.forEach(b -> System.out.println("Бюджет: id=" + b.getId() + ", category=" + (b.getCategory() != null ? b.getCategory().getName() : "null")));
        if (budgets.isEmpty()) {
            System.out.println("Предупреждение: Список бюджетов пуст для userId=" + userId + ", month=" + month);
        }
        return budgets;
    }

    public BigDecimal getSpentForBudget(Budget budget) {
        Objects.requireNonNull(budget, "Бюджет не может быть null");
        Objects.requireNonNull(budget.getCategory(), "Категория бюджета не может быть null");
        LocalDateTime startDate = budget.getMonth().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endDate = budget.getMonth().withDayOfMonth(budget.getMonth().lengthOfMonth()).atTime(LocalTime.MAX);

        List<Transaction> transactions = transactionRepository.findByUserIdAndCategoryIdAndDateBetween(
                budget.getUser().getId(),
                budget.getCategory().getId(),
                startDate,
                endDate
        );

        if (transactions.isEmpty()) {
            System.out.println("Предупреждение: Транзакции не найдены для бюджета '" + budget.getCategory().getName() + "' за " + budget.getMonth());
        }

        BigDecimal spent = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("Расходы по бюджету '" + budget.getCategory().getName() + "' за " + budget.getMonth() + ": " + spent);
        return spent;
    }

    public int getProgressPercentage(Budget budget) {
        Objects.requireNonNull(budget, "Бюджет не может быть null");
        BigDecimal limit = budget.getLimit();
        if (limit.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal spent = getSpentForBudget(budget);
        BigDecimal percentage = spent.divide(limit, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        int result = percentage.intValue();
        return Math.min(result, 100);
    }
}