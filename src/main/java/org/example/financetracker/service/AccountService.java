package org.example.financetracker.service;

import org.example.financetracker.entity.Account;
import org.example.financetracker.entity.User;
import org.example.financetracker.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(User user, String name) {
        Account account = new Account();
        account.setUser(user);
        account.setName(name);
        account.setBalance(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    public List<Account> getUserAccounts(User user) {
        return accountRepository.findByUser(user);
    }

    public void deleteAccount(Long accountId) {
        accountRepository.deleteById(accountId);
    }

    public Account saveAccount(Account account, User user) {
        if (!account.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Вы не можете редактировать чужой счёт");
        }
        return accountRepository.save(account);
    }

    public Account findByIdAndUser(Long accountId, User user) {
        return accountRepository.findById(accountId)
                .filter(account -> account.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new SecurityException("Счёт не найден или доступ запрещён"));
    }

    public List<Account> findAllByUser(User user) {
        return accountRepository.findByUser(user);
    }

    public Object getAccountsByUser(User user) {
        return accountRepository.findByUser(user);
    }

}