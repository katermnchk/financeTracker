package org.example.financetracker.controller;

import jakarta.validation.Valid;
import org.example.financetracker.dto.TransactionDTO;
import org.example.financetracker.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO,
                                                            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(transactionService.createTransaction(transactionDTO, username));
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(transactionService.getAllTransactionsByUser(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id,
                                                             Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(transactionService.getTransactionById(id, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id,
                                                  Authentication authentication) {
        String username = authentication.getName();
        transactionService.deleteTransaction(id, username);
        return ResponseEntity.noContent().build();
    }
}