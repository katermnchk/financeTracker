package org.example.financetracker.repository;

import org.example.financetracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Transaction findByTransactionId(Long transactionId);
}
