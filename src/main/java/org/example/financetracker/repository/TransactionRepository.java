package org.example.financetracker.repository;

import org.example.financetracker.entity.Account;
import org.example.financetracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccount(Account account);
}