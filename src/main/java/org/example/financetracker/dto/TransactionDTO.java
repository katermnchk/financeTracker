package org.example.financetracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class TransactionDTO {
    private Long id;
    @NotNull(message = "Сумма обязательна")
    private BigDecimal amount;
    @NotNull(message = "Тип обязателен")
    private String type;
    private String description;
    @NotNull(message = "Дата обязательна")
    private LocalDateTime date;
    @NotNull(message = "Счет обязателен")
    private Long accountId;
    @NotNull(message = "Категория обязательна")
    private Long categoryId;

    @Override
    public String toString() {
        return "TransactionDTO{" +
                "id=" + id +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", accountId=" + accountId +
                ", categoryId=" + categoryId +
                '}';
    }
}