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

    @NotNull(message = "ID счёта не может быть пустым")
    private Long accountId;

    @NotNull(message = "ID категории не может быть пустым")
    private Long categoryId;

    @NotNull(message = "Сумма не может быть пустой")
    @Positive(message = "Сумма должна быть больше 0")
    private BigDecimal amount;

    @NotNull(message = "Тип транзакции не может быть пустым")
    private String type;

    private String description;

    private LocalDateTime date;

    @Override
    public String toString() {
        return "TransactionDTO{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", categoryId=" + categoryId +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                '}';
    }
}