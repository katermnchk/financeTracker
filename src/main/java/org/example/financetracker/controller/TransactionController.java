package org.example.financetracker.controller;

import jakarta.validation.Valid;
import org.example.financetracker.dto.TransactionDTO;
import org.example.financetracker.entity.Category;
import org.example.financetracker.entity.Transaction;
import org.example.financetracker.entity.User;
import org.example.financetracker.security.CustomUserDetails;
import org.example.financetracker.service.AccountService;
import org.example.financetracker.service.CategoryService;
import org.example.financetracker.service.TransactionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final AccountService accountService;

    public TransactionController(TransactionService transactionService,
                                 CategoryService categoryService,
                                 AccountService accountService) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
        this.accountService = accountService;
    }

    @GetMapping("/add")
    public String showAddTransactionForm(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("User is not authenticated");
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        System.out.println("Authenticated user: " + user.getId() + ", username: " + user.getUsername());
        List<Category> availableCategories = categoryService.getAvailableCategories(user);
        System.out.println("Loading add form for user " + user.getId() + " with categories: " + availableCategories);
        model.addAttribute("transaction", new TransactionDTO());
        model.addAttribute("availableCategories", availableCategories);
        model.addAttribute("accounts", accountService.getAccountsByUser(user));
        return "transaction-form";
    }

    @PostMapping("/add")
    public String addTransaction(@ModelAttribute("transaction") @Valid TransactionDTO transactionDTO,
                                 BindingResult result,
                                 Authentication authentication,
                                 Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        if (result.hasErrors()) {
            List<Category> availableCategories = categoryService.getAvailableCategories(user);
            System.out.println("Validation errors for user " + user.getId() + " with categories: " + availableCategories);
            model.addAttribute("availableCategories", availableCategories);
            model.addAttribute("accounts", accountService.getAccountsByUser(user));
            return "transaction-form";
        }
        transactionService.saveTransaction(transactionDTO, user);
        return "redirect:/dashboard";
    }

    @GetMapping("/edit/{id}")
    public String showEditTransactionForm(@PathVariable Long id, Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        Transaction transaction = transactionService.findByIdAndUser(id, user);
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setId(transaction.getId());
        transactionDTO.setAmount(transaction.getAmount());
        transactionDTO.setType(transaction.getType());
        transactionDTO.setDescription(transaction.getDescription());
        transactionDTO.setDate(transaction.getDate());
        transactionDTO.setAccountId(transaction.getAccount().getId());
        transactionDTO.setCategoryId(transaction.getCategory().getId());
        List<Category> availableCategories = categoryService.getAvailableCategories(user);
        System.out.println("Loading edit form for user " + user.getId() + " with categories: " + availableCategories);
        model.addAttribute("transaction", transactionDTO);
        model.addAttribute("availableCategories", availableCategories);
        model.addAttribute("accounts", accountService.getAccountsByUser(user));
        return "transaction-form";
    }

    @PostMapping("/edit/{id}")
    public String editTransaction(@PathVariable Long id,
                                  @ModelAttribute("transaction") @Valid TransactionDTO transactionDTO,
                                  BindingResult result,
                                  Authentication authentication,
                                  Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        if (result.hasErrors()) {
            List<Category> availableCategories = categoryService.getAvailableCategories(user);
            System.out.println("Validation errors for user " + user.getId() + " with categories: " + availableCategories);
            model.addAttribute("availableCategories", availableCategories);
            model.addAttribute("accounts", accountService.getAccountsByUser(user));
            return "transaction-form";
        }
        transactionDTO.setId(id);
        transactionService.updateTransaction(transactionDTO, user);
        return "redirect:/dashboard";
    }

    @PostMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable Long id, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        transactionService.deleteTransaction(id, user);
        return "redirect:/dashboard";
    }
}