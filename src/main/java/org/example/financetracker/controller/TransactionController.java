package org.example.financetracker.controller;

import org.example.financetracker.dto.TransactionDTO;
import org.example.financetracker.entity.User;
import org.example.financetracker.service.AccountService;
import org.example.financetracker.service.CategoryService;
import org.example.financetracker.service.TransactionService;
import org.example.financetracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping({"/add", "/edit/{id}"})
    public String showTransactionForm(@PathVariable(name = "id", required = false) Long id,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      Model model) {
        User currentUser = userService.findByEmail(userDetails.getUsername());

        TransactionDTO transactionDTO;
        if (id != null) {
            transactionDTO = transactionService.findTransactionDTOByIdAndUser(id, currentUser);
            if (transactionDTO == null) {
                return "redirect:/dashboard";
            }
        } else {
            transactionDTO = new TransactionDTO();
        }

        model.addAttribute("transaction", transactionDTO);
        model.addAttribute("accounts", accountService.findAllByUser(currentUser));
        model.addAttribute("categories", categoryService.findAllByUser(currentUser));
        model.addAttribute("transactions", transactionService.findAllByUser(currentUser));

        return "transaction-form";
    }

    @PostMapping("/add")
    public String addTransaction(@Valid @ModelAttribute("transaction") TransactionDTO transactionDTO,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        User currentUser = userService.findByEmail(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            model.addAttribute("accounts", accountService.findAllByUser(currentUser));
            model.addAttribute("categories", categoryService.findAllByUser(currentUser));
            model.addAttribute("transactions", transactionService.findAllByUser(currentUser));
            return "transaction-form";
        }

        transactionService.saveTransaction(transactionDTO, currentUser);
        return "redirect:/dashboard";
    }

    @PostMapping("/edit/{id}")
    public String editTransaction(@PathVariable Long id,
                                  @Valid @ModelAttribute("transaction") TransactionDTO transactionDTO,
                                  BindingResult bindingResult,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        User currentUser = userService.findByEmail(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            model.addAttribute("accounts", accountService.findAllByUser(currentUser));
            model.addAttribute("categories", categoryService.findAllByUser(currentUser));
            model.addAttribute("transactions", transactionService.findAllByUser(currentUser));
            return "transaction-form";
        }

        transactionService.updateTransaction(id, transactionDTO, currentUser);
        return "redirect:/dashboard";
    }

    @PostMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByEmail(userDetails.getUsername());
        transactionService.deleteTransaction(id, currentUser);
        return "redirect:/dashboard";
    }
}
