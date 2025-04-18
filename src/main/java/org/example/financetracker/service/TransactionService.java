package org.example.financetracker.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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
import java.time.LocalDateTime;
import java.util.List;

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
                .orElseThrow(() -> new IllegalArgumentException("Счет не найден с таким ID: " + transactionDTO.getAccountId()));

        Category category = categoryRepository.findById(transactionDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена с таким ID: " + transactionDTO.getCategoryId()));
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
            System.err.println("ОШибка сохранения транзакции: " + e.getMessage());
            throw new RuntimeException("ОШибка сохранения транзакции", e);
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
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + transactionDTO.getAccountId()));

        //откат баланса
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
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + transactionDTO.getCategoryId()));
        if (!category.getIsDefault() && (category.getUser() == null || !category.getUser().getId().equals(user.getId()))) {
            throw new SecurityException("Category is not accessible to this user");
        }

        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setAmount(newAmount);
        transaction.setType(newType);
        transaction.setDescription(transactionDTO.getDescription());
        LocalDateTime transactionDate = transactionDTO.getDate() != null ? transactionDTO.getDate() : LocalDateTime.now();
        System.out.println("Setting transaction date: " + transactionDate);
        transaction.setDate(transactionDate);

        try {
            transactionRepository.save(transaction);
            System.out.println("Transaction updated successfully: " + transaction);
        } catch (Exception e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            throw new RuntimeException("Failed to update transaction", e);
        }
    }

    @Transactional
    public void deleteTransaction(Long id, User user) {
        System.out.println("Deleting transaction with ID: " + id);
        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found or access denied"));

        //откат баланса
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
        System.out.println("Transaction deleted successfully: " + id);
    }

    public Transaction findByIdAndUser(Long id, User user) {
        return transactionRepository.findById(id)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found or access denied"));
    }

    public List<Transaction> getAccountTransactions(Account account) {
        return transactionRepository.findByAccount(account);
    }

    @Transactional
    public void createTransaction(@Valid TransactionDTO transactionDTO, User user) {
        saveTransaction(transactionDTO, user);
    }
}