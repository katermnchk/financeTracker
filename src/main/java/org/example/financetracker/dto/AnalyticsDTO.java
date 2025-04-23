package org.example.financetracker.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class AnalyticsDTO {
    private String categoryName;
    private BigDecimal amount;
    private String type;

    public AnalyticsDTO(String categoryName, BigDecimal amount, String type) {
        this.categoryName = categoryName;
        this.amount = amount;
        this.type = type;
    }

    @Override
    public String toString() {
        return "AnalyticsDTO{" +
                "categoryName='" + categoryName + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                '}';
    }

}