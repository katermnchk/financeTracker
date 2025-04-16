package org.example.financetracker.service;

import org.example.financetracker.dto.TransactionDTO;
import org.example.financetracker.entity.Account;
import org.example.financetracker.entity.Category;
import org.example.financetracker.entity.Transaction;
import org.example.financetracker.entity.User;
import org.example.financetracker.repository.AccountRepository;
import org.example.financetracker.repository.CategoryRepository;
import org.example.financetracker.repository.TransactionRepository;
import org.example.financetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              CategoryRepository categoryRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public void createTransaction(TransactionDTO dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Account account = accountRepository.findById(dto.getAccountId())
                .filter(a -> a.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Счёт не найден"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setDescription(dto.getDescription());
        transaction.setDate(LocalDateTime.now());
        transaction.setCategory(category);

        BigDecimal newBalance = account.getBalance();
        if ("INCOME".equals(dto.getType())) {
            newBalance = newBalance.add(dto.getAmount());
        } else if ("EXPENSE".equals(dto.getType())) {
            newBalance = newBalance.subtract(dto.getAmount());
        }
        account.setBalance(newBalance);

        accountRepository.save(account);
        transactionRepository.save(transaction);
    }

    public List<Transaction> getAccountTransactions(Account account) {
        return transactionRepository.findByAccount(account);
    }


    public TransactionDTO getTransactionById(Long id, String username) {
        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getAccount().getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Транзакция не найдена"));

        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setAccountId(transaction.getAccount().getId());
        dto.setCategoryId(transaction.getCategory().getId());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setType(transaction.getType());
        return dto;
    }

    public void deleteTransaction(Long id, String username) {
        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getAccount().getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Транзакция не найдена"));

        Account account = transaction.getAccount();
        BigDecimal newBalance = account.getBalance();
        if ("INCOME".equals(transaction.getType())) {
            newBalance = newBalance.subtract(transaction.getAmount());
        } else if ("EXPENSE".equals(transaction.getType())) {
            newBalance = newBalance.add(transaction.getAmount());
        }

        account.setBalance(newBalance);
        accountRepository.save(account);

        transactionRepository.delete(transaction);
    }


}
