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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              CategoryRepository categoryRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public TransactionDTO createTransaction(TransactionDTO dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new RuntimeException("Счет не найден"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Счет не принадлежит пользователю");
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setDescription(dto.getDescription());
        transaction.setDate(dto.getDate());
        transaction.setAccount(account);
        transaction.setUser(user);

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Категория не найдена"));
            transaction.setCategory(category);
        }

        BigDecimal newBalance = "INCOME".equals(dto.getType())
                ? account.getBalance().add(dto.getAmount())
                : account.getBalance().subtract(dto.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        transaction = transactionRepository.save(transaction);
        dto.setId(transaction.getId());
        return dto;
    }

    public List<TransactionDTO> getAllTransactionsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return transactionRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO getTransactionById(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Транзакция не найдена"));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Транзакция не принадлежит пользователю");
        }
        return toDTO(transaction);
    }

    public void deleteTransaction(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Транзакция не найдена"));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Транзакция не принадлежит пользователю");
        }

        Account account = transaction.getAccount();
        BigDecimal newBalance = "INCOME".equals(transaction.getType())
                ? account.getBalance().subtract(transaction.getAmount())
                : account.getBalance().add(transaction.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        transactionRepository.deleteById(id);
    }

    private TransactionDTO toDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setDescription(transaction.getDescription());
        dto.setDate(transaction.getDate());
        dto.setAccountId(transaction.getAccount().getId());
        if (transaction.getCategory() != null) {
            dto.setCategoryId(transaction.getCategory().getId());
        }
        return dto;
    }
}