package org.example.financetracker.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;
    private LocalDateTime date;

    @ManyToOne
    private Account account;

    @ManyToOne
    private Category category;

    @ManyToOne
    private User user;
}
