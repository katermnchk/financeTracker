package org.example.financetracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
public class BudgetDTO {

    @NotNull(message = "ID категории не может быть пустым")
    private Long categoryId;

    @NotNull(message = "Лимит не может быть пустым")
    @Positive(message = "Лимит должен быть больше нуля")
    private BigDecimal limit;

    @NotNull(message = "Месяц не может быть пустым")
    private LocalDate month;

}