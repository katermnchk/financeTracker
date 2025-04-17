package org.example.financetracker.controller;

import jakarta.validation.Valid;
import org.example.financetracker.dto.TransactionDTO;
import org.example.financetracker.entity.Account;
import org.example.financetracker.entity.Transaction;
import org.example.financetracker.entity.User;
import org.example.financetracker.repository.AccountRepository;
import org.example.financetracker.repository.CategoryRepository;
import org.example.financetracker.repository.UserRepository;
import org.example.financetracker.service.AccountService;
import org.example.financetracker.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public DashboardController(UserRepository userRepository, AccountService accountService,
                               TransactionService transactionService, AccountRepository accountRepository,
                               CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login?error";

        String username = userDetails.getUsername();
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) return "redirect:/login?error";

        User user = userOptional.get();
        List<Account> accounts = accountService.getUserAccounts(user);

        model.addAttribute("username", username);
        model.addAttribute("accounts", accounts);
        model.addAttribute("newAccount", new Account());

        return "dashboard";
    }

    @PostMapping("/account/add")
    public String addAccount(@AuthenticationPrincipal UserDetails userDetails,
                             @ModelAttribute("newAccount") Account newAccount) {
        String username = userDetails.getUsername();
        userRepository.findByUsername(username).ifPresent(
                user -> accountService.createAccount(user, newAccount.getName()));
        return "redirect:/dashboard";
    }

    @PostMapping("/account/delete/{id}")
    public String deleteAccount(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        userRepository.findByUsername(username).ifPresent(user -> {
            accountService.getUserAccounts(user).stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .ifPresent(account -> accountService.deleteAccount(id));
        });
        return "redirect:/dashboard";
    }

    @GetMapping("/transactions/{accountId}")
    public String viewTransactions(@PathVariable Long accountId, Model model,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login?error";

        String username = userDetails.getUsername();
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) return "redirect:/login?error";

        User user = userOptional.get();
        Optional<Account> accountOptional = accountService.getUserAccounts(user)
                .stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst();

        if (accountOptional.isEmpty()) return "redirect:/dashboard?error=accountNotFound";

        Account account = accountOptional.get();
        List<Transaction> transactions = transactionService.getAccountTransactions(account);
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAccountId(accountId);

        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);
        model.addAttribute("transaction", transactionDTO);
        model.addAttribute("accounts", accountRepository.findByUser(user));
        model.addAttribute("categories", categoryRepository.findByUser(user));

        return "transaction-form";
    }

    @PostMapping("/transaction/add/{accountId}")
    public String addTransaction(@PathVariable Long accountId,
                                 @Valid @ModelAttribute("transaction") TransactionDTO transactionDTO,
                                 BindingResult result,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        if (userDetails == null) return "redirect:/login?error";

        String username = userDetails.getUsername();
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) return "redirect:/login?error";

        User user = userOptional.get();
        if (result.hasErrors()) {
            Optional<Account> accountOptional = accountService.getUserAccounts(user)
                    .stream()
                    .filter(a -> a.getId().equals(accountId))
                    .findFirst();
            if (accountOptional.isEmpty()) return "redirect:/dashboard?error=accountNotFound";

            Account account = accountOptional.get();
            model.addAttribute("account", account);
            model.addAttribute("transactions", transactionService.getAccountTransactions(account));
            model.addAttribute("accounts", accountRepository.findByUser(user));
            model.addAttribute("categories", categoryRepository.findByUser(user));
            return "transaction-form";
        }

        transactionDTO.setAccountId(accountId);
        transactionService.createTransaction(transactionDTO, userDetails.getUsername());
        return "redirect:/transactions/" + accountId;
    }
    @PostMapping("/transaction/add/{accountId}")
   public String addTransaction(@PathVariable Long accountId,
                                @Valid @ModelAttribute("transaction") TransactionDTO transactionDTO,
                                BindingResult result,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
       if (userDetails == null) return "redirect:/login?error";

       String username = userDetails.getUsername();
       Optional<User> userOptional = userRepository.findByUsername(username);
       if (userOptional.isEmpty()) return "redirect:/login?error";

       User user = userOptional.get();

       if (result.hasErrors()) {
           Optional<Account> accountOptional = accountService.getUserAccounts(user)
                   .stream()
                   .filter(a -> a.getId().equals(accountId))
                   .findFirst();
           if (accountOptional.isEmpty()) return "redirect:/dashboard?error=accountNotFound";

           Account account = accountOptional.get();
           model.addAttribute("account", account);
           model.addAttribute("transactions", transactionService.getAccountTransactions(account));
           model.addAttribute("accounts", accountRepository.findByUser(user));
           model.addAttribute("categories", categoryRepository.findByUser(user));
           return "transaction-form";
       }

       transactionDTO.setAccountId(accountId);

       transactionService.createTransaction(transactionDTO, user);

       return "redirect:/transactions/" + accountId;
   }



}