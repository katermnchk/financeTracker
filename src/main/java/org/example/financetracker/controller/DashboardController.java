package org.example.financetracker.controller;

import org.example.financetracker.dto.TransactionDTO;
import org.example.financetracker.entity.Account;
import org.example.financetracker.entity.Transaction;
import org.example.financetracker.entity.User;
import org.example.financetracker.repository.UserRepository;
import org.example.financetracker.service.AccountService;
import org.example.financetracker.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @Autowired
    public DashboardController(UserRepository userRepository, AccountService accountService, TransactionService transactionService) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login?error";
        }

        String username = userDetails.getUsername();
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return "redirect:/login?error";
        }

        User user = userOptional.get();
        List<Account> accounts = accountService.getUserAccounts(user);
        model.addAttribute("username", username);
        model.addAttribute("accounts", accounts);
        model.addAttribute("newAccount", new Account());
        return "dashboard";
    }

    @PostMapping("/account/add")
    public String addAccount(@AuthenticationPrincipal UserDetails userDetails, @ModelAttribute("newAccount") Account newAccount) {
        String username = userDetails.getUsername();
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            accountService.createAccount(userOptional.get(), newAccount.getName());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/account/delete/{id}")
    public String deleteAccount(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            Optional<Account> account = accountService.getUserAccounts(userOptional.get())
                    .stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst();
            if (account.isPresent()) {
                accountService.deleteAccount(id);
            }
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/transactions/{accountId}")
    public String viewTransactions(@PathVariable Long accountId, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login?error";
        }

        String username = userDetails.getUsername();
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return "redirect:/login?error";
        }

        Optional<Account> accountOptional = accountService.getUserAccounts(userOptional.get())
                .stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst();

        if (accountOptional.isEmpty()) {
            return "redirect:/dashboard?error=accountNotFound";
        }

        Account account = accountOptional.get();
        List<Transaction> transactions = transactionService.getAccountTransactions(account);
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAccountId(accountId);

        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);
        model.addAttribute("transaction", transactionDTO);
        return "transaction-form";
    }

    @PostMapping("/transaction/add/{accountId}")
    public String addTransaction(@PathVariable Long accountId,
                                 @ModelAttribute("transaction") TransactionDTO transactionDTO,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login?error";
        }

        transactionDTO.setAccountId(accountId);
        transactionService.createTransaction(transactionDTO, userDetails.getUsername());
        return "redirect:/transactions/" + accountId;
    }
}