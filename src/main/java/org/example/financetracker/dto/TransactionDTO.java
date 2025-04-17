package org.example.financetracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class TransactionDTO {

    private Long id;

    @NotNull(message = "Счёт обязателен")
    private Long accountId;

    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;

    @NotNull(message = "Тип обязателен")
    private String type;

    private String description;

    @NotNull(message = "Дата обязательна")
    private LocalDateTime date;

    @NotNull(message = "Категория обязательна")
    private Long categoryId;

}
