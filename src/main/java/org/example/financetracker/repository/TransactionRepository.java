package org.example.financetracker.repository;

import org.example.financetracker.entity.Account;
import org.example.financetracker.entity.Transaction;
import org.example.financetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccount(Account account);

    List<Transaction> findByAccountId(Long id);

    List<Transaction> findAllByUser(User user);

    Optional<Transaction> findByIdAndUser(Long id, User user);
}