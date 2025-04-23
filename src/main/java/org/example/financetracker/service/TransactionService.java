package org.example.financetracker.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.example.financetracker.dto.AnalyticsDTO;
import org.example.financetracker.dto.TransactionDTO;
import org.example.financetracker.entity.Account;
import org.example.financetracker.entity.Category;
import org.example.financetracker.entity.Transaction;
import org.example.financetracker.entity.User;
import org.example.financetracker.repository.AccountRepository;
import org.example.financetracker.repository.CategoryRepository;
import org.example.financetracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public void saveTransaction(@Valid TransactionDTO transactionDTO, User user) {
        System.out.println("Сохранение транзакции с DTO: " + transactionDTO);
        if (transactionDTO == null) {
            throw new IllegalArgumentException("TransactionDTO не может быть null");
        }
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }

        Account account = accountRepository.findById(transactionDTO.getAccountId())
                .filter(a -> a.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Счет не найден с ID: " + transactionDTO.getAccountId()));

        Category category = categoryRepository.findById(transactionDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена с ID: " + transactionDTO.getCategoryId()));
        if (!category.getIsDefault() && (category.getUser() == null || !category.getUser().getId().equals(user.getId()))) {
            throw new SecurityException("Категория недоступна для этого пользователя");
        }

        BigDecimal amount = transactionDTO.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше нуля");
        }
        String type = transactionDTO.getType();
        if ("INCOME".equals(type)) {
            account.setBalance(account.getBalance().add(amount));
        } else if ("EXPENSE".equals(type)) {
            BigDecimal newBalance = account.getBalance().subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Недостаточно средств на счёте");
            }
            account.setBalance(newBalance);
        } else {
            throw new IllegalArgumentException("Неверный тип транзакции: " + type);
        }
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setDescription(transactionDTO.getDescription());
        LocalDateTime transactionDate = transactionDTO.getDate() != null ? transactionDTO.getDate() : LocalDateTime.now();
        System.out.println("Установка даты транзакции: " + transactionDate);
        transaction.setDate(transactionDate);
        transaction.setUser(user);

        try {
            transactionRepository.save(transaction);
            System.out.println("Транзакция успешно сохранена: " + transaction);
        } catch (Exception e) {
            System.err.println("Ошибка сохранения транзакции: " + e.getMessage());
            throw new RuntimeException("Ошибка сохранения транзакции", e);
        }
    }

    @Transactional
    public void updateTransaction(@Valid TransactionDTO transactionDTO, User user) {
        System.out.println("Обновление транзакции с DTO: " + transactionDTO);
        if (transactionDTO == null || transactionDTO.getId() == null) {
            throw new IllegalArgumentException("TransactionDTO или ID не могут быть null");
        }

        Transaction transaction = transactionRepository.findById(transactionDTO.getId())
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Транзакция не найдена"));

        Account account = accountRepository.findById(transactionDTO.getAccountId())
                .filter(a -> a.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Счет не найден с ID: " + transactionDTO.getAccountId()));

        // Откат баланса
        BigDecimal oldAmount = transaction.getAmount();
        String oldType = transaction.getType();
        if ("INCOME".equals(oldType)) {
            account.setBalance(account.getBalance().subtract(oldAmount));
        } else if ("EXPENSE".equals(oldType)) {
            account.setBalance(account.getBalance().add(oldAmount));
        }

        BigDecimal newAmount = transactionDTO.getAmount();
        if (newAmount == null || newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше нуля");
        }
        String newType = transactionDTO.getType();
        if ("INCOME".equals(newType)) {
            account.setBalance(account.getBalance().add(newAmount));
        } else if ("EXPENSE".equals(newType)) {
            BigDecimal newBalance = account.getBalance().subtract(newAmount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Недостаточно средств на счёте");
            }
            account.setBalance(newBalance);
        } else {
            throw new IllegalArgumentException("Неверный тип транзакции: " + newType);
        }
        accountRepository.save(account);

        Category category = categoryRepository.findById(transactionDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена с ID: " + transactionDTO.getCategoryId()));
        if (!category.getIsDefault() && (category.getUser() == null || !category.getUser().getId().equals(user.getId()))) {
            throw new SecurityException("Категория недоступна для этого пользователя");
        }

        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setAmount(newAmount);
        transaction.setType(newType);
        transaction.setDescription(transactionDTO.getDescription());
        LocalDateTime transactionDate = transactionDTO.getDate() != null ? transactionDTO.getDate() : LocalDateTime.now();
        System.out.println("Установка даты транзакции: " + transactionDate);
        transaction.setDate(transactionDate);

        try {
            transactionRepository.save(transaction);
            System.out.println("Транзакция успешно обновлена: " + transaction);
        } catch (Exception e) {
            System.err.println("Ошибка обновления транзакции: " + e.getMessage());
            throw new RuntimeException("Ошибка обновления транзакции", e);
        }
    }

    @Transactional
    public void deleteTransaction(Long id, User user) {
        System.out.println("Удаление транзакции с ID: " + id);
        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Транзакция не найдена или доступ запрещён"));

        // Откат баланса
        Account account = transaction.getAccount();
        BigDecimal amount = transaction.getAmount();
        String type = transaction.getType();
        if ("INCOME".equals(type)) {
            account.setBalance(account.getBalance().subtract(amount));
        } else if ("EXPENSE".equals(type)) {
            account.setBalance(account.getBalance().add(amount));
        }
        accountRepository.save(account);

        transactionRepository.delete(transaction);
        System.out.println("Транзакция успешно удалена: " + id);
    }

    public Transaction findByIdAndUser(Long id, User user) {
        return transactionRepository.findById(id)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Транзакция не найдена или доступ запрещён"));
    }

    public List<Transaction> getAccountTransactions(Account account) {
        return transactionRepository.findByAccount(account);
    }

    @Transactional
    public void createTransaction(@Valid TransactionDTO transactionDTO, User user) {
        saveTransaction(transactionDTO, user);
    }

    public Map<String, Map<String, BigDecimal>> getIncomeAndExpensesByCategory(User user) {
        List<Transaction> transactions = transactionRepository.findByUser(user);
        return transactions.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.groupingBy(
                                Transaction::getType,
                                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                        )
                ));
    }

    public Map<String, BigDecimal> getExpensesByCategory(User user) {
        List<Transaction> transactions = transactionRepository.findByUser(user);
        return transactions.stream()
                .filter(t -> t.getCategory() != null && "EXPENSE".equals(t.getType()))
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }

    public List<AnalyticsDTO> getAnalytics(Long userId, LocalDateTime start, LocalDateTime end, String type) {
        Objects.requireNonNull(userId, "ID пользователя не может быть null");
        Objects.requireNonNull(start, "Начальная дата не может быть null");
        Objects.requireNonNull(end, "Конечная дата не может быть null");

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(
                userId,
                start,
                end
        );
        System.out.println("Транзакции для аналитики userId=" + userId + ", start=" + start + ", end=" + end + ", type=" + (type != null ? type : "all") + ": " + transactions);

        if (transactions.isEmpty()) {
            System.out.println("Предупреждение: Транзакции не найдены для userId=" + userId + ", period=" + start + " to " + end);
        }

        Map<String, AnalyticsDTO> analyticsMap = new HashMap<>();
        for (Transaction t : transactions) {
            if (type != null && !type.isEmpty() && !t.getType().equals(type)) {
                continue;
            }
            String categoryName = t.getCategory() != null ? t.getCategory().getName() : "Без категории";
            analyticsMap.compute(categoryName, (key, value) -> {
                if (value == null) {
                    return new AnalyticsDTO(categoryName, t.getAmount(), t.getType());
                } else {
                    value.setAmount(value.getAmount().add(t.getAmount()));
                    return value;
                }
            });
        }

        List<AnalyticsDTO> result = new ArrayList<>(analyticsMap.values());
        System.out.println("Результат аналитики: " + result);
        return result;
    }
}