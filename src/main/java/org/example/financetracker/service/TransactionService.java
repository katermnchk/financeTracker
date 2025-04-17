package org.example.financetracker.service;

import jakarta.validation.Valid;
import org.example.financetracker.dto.TransactionDTO;
import org.example.financetracker.entity.*;
import org.example.financetracker.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.financetracker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public List<Transaction> findAllByUser(User user) {
        return transactionRepository.findAllByUser(user);
    }

    public TransactionDTO findTransactionDTOByIdAndUser(Long id, User user) {
        Optional<Transaction> transactionOpt = transactionRepository.findByIdAndUser(id, user);
        return transactionOpt.map(this::mapToDTO).orElse(null);
    }

    @Transactional
    public void saveTransaction(TransactionDTO dto, User user) {
        Transaction transaction = mapToEntity(dto, user);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void updateTransaction(Long id, TransactionDTO dto, User user) {
        Transaction existing = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Транзакция не найдена или недоступна"));

        existing.setAccount(accountService.findByIdAndUser(dto.getAccountId(), user));
        existing.setCategory(categoryService.findByIdAndUser(dto.getCategoryId(), user));
        existing.setAmount(dto.getAmount());
        existing.setType(dto.getType());
        existing.setDescription(dto.getDescription());
        existing.setDate(dto.getDate());

        transactionRepository.save(existing);
    }

    @Transactional
    public void deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Транзакция не найдена или недоступна"));
        transactionRepository.delete(transaction);
    }

    private Transaction mapToEntity(TransactionDTO dto, User user) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAccount(accountService.findByIdAndUser(dto.getAccountId(), user));
        transaction.setCategory(categoryService.findByIdAndUser(dto.getCategoryId(), user));
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setDescription(dto.getDescription());
        transaction.setDate(dto.getDate());
        return transaction;
    }

    private TransactionDTO mapToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setAccountId(transaction.getAccount().getId());
        dto.setCategoryId(transaction.getCategory().getId());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setDescription(transaction.getDescription());
        dto.setDate(transaction.getDate());
        return dto;
    }

    public List<Transaction> getAccountTransactions(Account account) {
        return transactionRepository.findByAccount(account);
    }

   /* @Transactional
    public void createTransaction(@Valid TransactionDTO transactionDTO, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Account account = accountService.findByIdAndUser(transactionDTO.getAccountId(), user);
        Category category = categoryService.findByIdAndUser(transactionDTO.getCategoryId(), user);

        Transaction transaction = mapToEntity(transactionDTO, user);

        transactionRepository.save(transaction);
    }*/
   @Transactional
   public void createTransaction(@Valid TransactionDTO transactionDTO, User user) {
       Account account = accountService.findByIdAndUser(transactionDTO.getAccountId(), user);
       Category category = categoryService.findByIdAndUser(transactionDTO.getCategoryId(), user);

       Transaction transaction = mapToEntity(transactionDTO, user);

       transactionRepository.save(transaction);
   }


}
