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
        System.out.println("Saving transaction with DTO: " + transactionDTO);
        if (transactionDTO == null) {
            throw new IllegalArgumentException("TransactionDTO cannot be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        Transaction transaction = new Transaction();
        transaction.setAccount(accountRepository.findById(transactionDTO.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + transactionDTO.getAccountId())));
        Category category = categoryRepository.findById(transactionDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + transactionDTO.getCategoryId()));
        if (!category.getIsDefault() && (category.getUser() == null || !category.getUser().getId().equals(user.getId()))) {
            throw new SecurityException("Category is not accessible to this user");
        }
        transaction.setCategory(category);
        if (transactionDTO.getAmount() == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        transaction.setAmount(transactionDTO.getAmount());
        if (transactionDTO.getType() == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        transaction.setType(transactionDTO.getType());
        transaction.setDescription(transactionDTO.getDescription());
        LocalDateTime transactionDate = transactionDTO.getDate() != null ? transactionDTO.getDate() : LocalDateTime.now();
        System.out.println("Setting transaction date: " + transactionDate);
        transaction.setDate(transactionDate);
        transaction.setUser(user);
        try {
            transactionRepository.save(transaction);
            System.out.println("Transaction saved successfully: " + transaction);
        } catch (Exception e) {
            System.err.println("Error saving transaction: " + e.getMessage());
            throw new RuntimeException("Failed to save transaction", e);
        }
    }

    @Transactional
    public void updateTransaction(@Valid TransactionDTO transactionDTO, User user) {
        System.out.println("Updating transaction with DTO: " + transactionDTO);
        if (transactionDTO == null || transactionDTO.getId() == null) {
            throw new IllegalArgumentException("TransactionDTO or ID cannot be null");
        }
        Transaction transaction = transactionRepository.findById(transactionDTO.getId())
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found or access denied"));
        transaction.setAccount(accountRepository.findById(transactionDTO.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + transactionDTO.getAccountId())));
        Category category = categoryRepository.findById(transactionDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + transactionDTO.getCategoryId()));

        if (!category.getIsDefault() && (category.getUser() == null || !category.getUser().getId().equals(user.getId()))) {
            throw new SecurityException("Category is not accessible to this user");
        }
        transaction.setCategory(category);
        if (transactionDTO.getAmount() == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        transaction.setAmount(transactionDTO.getAmount());
        if (transactionDTO.getType() == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        transaction.setType(transactionDTO.getType());
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
        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found or access denied"));
        transactionRepository.delete(transaction);
    }

    public Transaction findByIdAndUser(Long id, User user) {
        return transactionRepository.findById(id)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found or access denied"));
    }

    public List<Transaction> getAccountTransactions(Account account) {
        return transactionRepository.findByAccount(account);
    }

    public void createTransaction(@Valid TransactionDTO transactionDTO, User user) {

    }
}