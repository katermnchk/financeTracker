package org.example.financetracker.controller;

import jakarta.validation.Valid;
import org.example.financetracker.dto.TransactionDTO;
import org.example.financetracker.entity.*;
import org.example.financetracker.repository.AccountRepository;
import org.example.financetracker.repository.CategoryRepository;
import org.example.financetracker.repository.UserRepository;
import org.example.financetracker.security.CustomUserDetails;
import org.example.financetracker.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final GoalService goalService;

    @Autowired
    public DashboardController(UserRepository userRepository, AccountService accountService,
                               TransactionService transactionService, AccountRepository accountRepository,
                               CategoryRepository categoryRepository, CategoryService categoryService,
                               GoalService goalService) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.goalService = goalService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login?error";

        User user = userDetails.getUser();
        List<Account> accounts = accountService.getUserAccounts(user);

        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        Map<String, Map<String, BigDecimal>> incomesByCategory = transactionService.getIncomeAndExpensesByCategory(user);
        //List<Budget> budgets = budgetService.getBudgetsForMonth(user.getId(), LocalDate.now().withDayOfMonth(1));
        List<Goal> goals = goalService.getGoalsForMonth(user.getId(), currentMonth);

        System.out.println("Передача данных в dashboard.html: username=" + user.getUsername() +
                ", accounts=" + accounts +
                ", totalBalance=" + totalBalance +
                ", barChartData=" + incomesByCategory +
                ", goals=" + goals +
                ", budgets type=" + (goals != null ? goals.getClass().getName() : "null"));

        model.addAttribute("username", user.getUsername());
        model.addAttribute("accounts", accounts);
        model.addAttribute("newAccount", new Account());
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("barChartData", incomesByCategory);
        model.addAttribute("goals", goals != null ? goals : List.of());
        model.addAttribute("goalService", goalService);

        return "dashboard";
    }

    @PostMapping("/account/add")
    public String addAccount(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @ModelAttribute("newAccount") @Valid Account newAccount) {
        User user = userDetails.getUser();
        accountService.createAccount(user, newAccount.getName());
        return "redirect:/dashboard";
    }

    @PostMapping("/account/delete/{id}")
    public String deleteAccount(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        accountService.getUserAccounts(user).stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .ifPresent(account -> accountService.deleteAccount(id));
        return "redirect:/dashboard";
    }

   /* @GetMapping("/transactions/{accountId}")
    public String viewTransactions(@PathVariable Long accountId, Model model,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login?error";

        User user = userDetails.getUser();
        Optional<Account> accountOptional = accountService.getUserAccounts(user)
                .stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst();

        if (accountOptional.isEmpty()) return "redirect:/dashboard?error=accountNotFound";

        Account account = accountOptional.get();
        List<Transaction> transactions = transactionService.getAccountTransactions(account);
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAccountId(accountId);

        List<Category> availableCategories = categoryService.getAvailableCategories(user);
        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);
        model.addAttribute("transaction", transactionDTO);
        model.addAttribute("availableCategories", availableCategories);
        model.addAttribute("accounts", accountRepository.findByUser(user));
        model.addAttribute("categories", categoryRepository.findByUser(user));

        return "transaction-form";
    }*/

    @PostMapping("/transaction/add/{accountId}")
    public String addTransaction(@PathVariable Long accountId,
                                 @Valid @ModelAttribute("transaction") TransactionDTO transactionDTO,
                                 BindingResult result,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        if (userDetails == null) return "redirect:/login?error";

        User user = userDetails.getUser();
        if (result.hasErrors()) {
            List<Category> availableCategories = categoryService.getAvailableCategories(user);
            Optional<Account> accountOptional = accountService.getUserAccounts(user)
                    .stream()
                    .filter(a -> a.getId().equals(accountId))
                    .findFirst();
            if (accountOptional.isEmpty()) return "redirect:/dashboard?error=accountNotFound";

            Account account = accountOptional.get();
            model.addAttribute("account", account);
            model.addAttribute("transactions", transactionService.getAccountTransactions(account));
            model.addAttribute("accounts", accountRepository.findByUser(user));
            model.addAttribute("availableCategories", availableCategories);
            model.addAttribute("categories", categoryRepository.findByUser(user));
            return "transaction-form";
        }

        transactionDTO.setAccountId(accountId);
        transactionService.createTransaction(transactionDTO, user);
        return "redirect:/transactions/" + accountId;
    }
}