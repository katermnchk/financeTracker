package org.example.financetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionDTO {

    private Long id;

    @NotNull(message = "Сумма не может быть пустой")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;

    @NotBlank(message = "Тип транзакции обязателен")
    private String type;

    private String description;

    @NotNull(message = "Дата обязательна")
    private LocalDateTime date;

    @NotNull(message = "Счет обязателен")
    private Long accountId;

    private Long categoryId;
}