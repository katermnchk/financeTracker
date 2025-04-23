package org.example.financetracker.repository;

import org.example.financetracker.entity.Account;
import org.example.financetracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findByAccount(Account account);
    List<Transaction> findByUser(org.example.financetracker.entity.User user);
    List<Transaction> findByUserIdAndCategoryIdAndDateBetween(Long userId, Long categoryId, LocalDateTime startDate, LocalDateTime endDate);
}