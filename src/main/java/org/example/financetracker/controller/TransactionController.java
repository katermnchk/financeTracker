package org.example.financetracker.controller;

import jakarta.validation.Valid;
import org.example.financetracker.dto.TransactionDTO;
import org.example.financetracker.entity.Account;
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
            System.out.println("Пользователь не аутентифицирован");
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        System.out.println("Аутентифицирован пользователь: " + user.getId() + ", username: " + user.getUsername());
        List<Category> availableCategories = categoryService.getAvailableCategories(user);
        System.out.println("Загрузка формы добавления для пользователя " + user.getId() + " с категориями: " + availableCategories);
        model.addAttribute("transaction", new TransactionDTO());
        model.addAttribute("availableCategories", availableCategories);
        model.addAttribute("accounts", accountService.getAccountsByUser(user));
        return "transaction-form";
    }

    @GetMapping("/add/{accountId}")
    public String showAddTransactionFormForAccount(@PathVariable Long accountId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Пользователь не аутентифицирован");
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        System.out.println("Аутентифицирован пользователь: " + user.getId() + ", username: " + user.getUsername());
        List<Category> availableCategories = categoryService.getAvailableCategories(user);
        System.out.println("Загрузка формы добавления для пользователя " + user.getId() + " с категориями: " + availableCategories);
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAccountId(accountId);
        model.addAttribute("transaction", transactionDTO);
        model.addAttribute("availableCategories", availableCategories);
        model.addAttribute("accounts", accountService.getAccountsByUser(user));
        return "transaction-form";
    }

    @PostMapping("/add")
    public String addTransaction(@ModelAttribute("transaction") @Valid TransactionDTO transactionDTO,
                                 BindingResult result,
                                 Authentication authentication,
                                 Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Пользователь не аутентифицирован");
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        if (result.hasErrors()) {
            List<Category> availableCategories = categoryService.getAvailableCategories(user);
            System.out.println("Ошибки валидации для пользователя " + user.getId() + ": " + result.getAllErrors());
            model.addAttribute("availableCategories", availableCategories);
            model.addAttribute("accounts", accountService.getAccountsByUser(user));
            model.addAttribute("error", "Пожалуйста, исправьте ошибки в форме");
            return "transaction-form";
        }
        try {
            transactionService.saveTransaction(transactionDTO, user);
            return "redirect:/dashboard";
        } catch (IllegalArgumentException | SecurityException e) {
            List<Category> availableCategories = categoryService.getAvailableCategories(user);
            System.out.println("Ошибка при добавлении транзакции для пользователя " + user.getId() + ": " + e.getMessage());
            model.addAttribute("availableCategories", availableCategories);
            model.addAttribute("accounts", accountService.getAccountsByUser(user));
            model.addAttribute("error", e.getMessage());
            return "transaction-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditTransactionForm(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Пользователь не аутентифицирован");
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        try {
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
            System.out.println("Загрузка формы редактирования для пользователя " + user.getId() + " с категориями: " + availableCategories);
            model.addAttribute("transaction", transactionDTO);
            model.addAttribute("availableCategories", availableCategories);
            model.addAttribute("accounts", accountService.getAccountsByUser(user));
            return "transaction-form";
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка при загрузке формы редактирования для пользователя " + user.getId() + ": " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/edit/{id}")
    public String editTransaction(@PathVariable Long id,
                                  @ModelAttribute("transaction") @Valid TransactionDTO transactionDTO,
                                  BindingResult result,
                                  Authentication authentication,
                                  Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Пользователь не аутентифицирован");
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        if (result.hasErrors()) {
            List<Category> availableCategories = categoryService.getAvailableCategories(user);
            System.out.println("Ошибки валидации для пользователя " + user.getId() + ": " + result.getAllErrors());
            model.addAttribute("availableCategories", availableCategories);
            model.addAttribute("accounts", accountService.getAccountsByUser(user));
            model.addAttribute("error", "Пожалуйста, исправьте ошибки в форме");
            return "transaction-form";
        }
        try {
            transactionDTO.setId(id);
            transactionService.updateTransaction(transactionDTO, user);
            return "redirect:/dashboard";
        } catch (IllegalArgumentException | SecurityException e) {
            List<Category> availableCategories = categoryService.getAvailableCategories(user);
            System.out.println("Ошибка при редактировании транзакции для пользователя " + user.getId() + ": " + e.getMessage());
            model.addAttribute("availableCategories", availableCategories);
            model.addAttribute("accounts", accountService.getAccountsByUser(user));
            model.addAttribute("error", e.getMessage());
            return "transaction-form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable Long id, Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Пользователь не аутентифицирован");
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        try {
            transactionService.deleteTransaction(id, user);
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка при удалении транзакции для пользователя " + user.getId() + ": " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/{id}")
    public String showAccountTransactions(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Пользователь не аутентифицирован");
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        try {
            Account account = accountService.findByIdAndUser(id, user);
            List<Transaction> transactions = transactionService.getAccountTransactions(account);
            System.out.println("Загрузка транзакций для счёта ID=" + id + " пользователя " + user.getId() + ": " + transactions);
            model.addAttribute("account", account);
            model.addAttribute("transactions", transactions);
            return "transactions";
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка при загрузке транзакций для счёта ID=" + id + " пользователя " + user.getId() + ": " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        }
    }
}