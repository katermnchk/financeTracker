package org.example.financetracker.controller;

import org.example.financetracker.entity.Account;
import org.example.financetracker.entity.User;
import org.example.financetracker.service.AccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/new")
    public String showAccountForm(Model model) {
        model.addAttribute("account", new Account());
        return "account_form";
    }

    @PostMapping
    public String saveAccount(@ModelAttribute Account account, @AuthenticationPrincipal User user) {
        accountService.saveAccount(account, user);
        return "redirect:/dashboard";
    }
}