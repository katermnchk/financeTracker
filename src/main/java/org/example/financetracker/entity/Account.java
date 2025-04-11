package org.example.financetracker.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Set;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private BigDecimal balance;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "account")
    private Set<Transaction> transactions;


}
