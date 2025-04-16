package org.example.financetracker.controller;

import jakarta.validation.Valid;
import org.example.financetracker.dto.TransactionDTO;
import org.example.financetracker.entity.User;
import org.example.financetracker.repository.AccountRepository;
import org.example.financetracker.repository.CategoryRepository;
import org.example.financetracker.service.TransactionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public TransactionController(TransactionService transactionService, AccountRepository accountRepository, CategoryRepository categoryRepository) {
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/new")
    public String showTransactionForm(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("transaction", new TransactionDTO());
        model.addAttribute("accounts", accountRepository.findByUser(user));
        model.addAttribute("categories", categoryRepository.findByUser(user));
        return "transaction-form";
    }

    @PostMapping
    public String saveTransaction(@Valid @ModelAttribute("transaction") TransactionDTO transaction, BindingResult result,
                                  @AuthenticationPrincipal User user, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("accounts", accountRepository.findByUser(user));
            model.addAttribute("categories", categoryRepository.findByUser(user));
            return "transaction-form";
        }
        transactionService.createTransaction(transaction, user.getUsername());
        return "redirect:/dashboard";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        TransactionDTO transaction = transactionService.getTransactionById(id, user.getUsername());
        model.addAttribute("transaction", transaction);
        model.addAttribute("accounts", accountRepository.findByUser(user));
        model.addAttribute("categories", categoryRepository.findByUser(user));
        return "transaction-form";
    }

    @PostMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable Long id, @AuthenticationPrincipal User user) {
        transactionService.deleteTransaction(id, user.getUsername());
        return "redirect:/dashboard";
    }

    @PostMapping("/{accountId}")
    public String addTransaction(@PathVariable Long accountId,
                                 @Valid @ModelAttribute("transaction") TransactionDTO transaction,
                                 BindingResult result, @AuthenticationPrincipal User user, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("accounts", accountRepository.findByUser(user));
            model.addAttribute("categories", categoryRepository.findByUser(user));
            return "transaction-form";
        }

        transaction.setAccountId(accountId);
        transactionService.createTransaction(transaction, user.getUsername());
        return "redirect:/transactions/" + accountId;
    }
}