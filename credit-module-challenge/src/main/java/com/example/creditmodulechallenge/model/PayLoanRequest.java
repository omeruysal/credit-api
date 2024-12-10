package com.example.creditmodulechallenge.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayLoanRequest {
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0", inclusive = false, message = "Amount must be greater than 0")
    private Double amount;
}