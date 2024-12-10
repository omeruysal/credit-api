package com.example.creditmodulechallenge.model;

import com.example.creditmodulechallenge.config.AllowedValue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateLoanRequest {
    @NotNull(message = "Customer Id cannot be blank")
    private Long customerId;
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0", inclusive = false, message = "Amount must be greater than 0")
    private Double amount;
    @DecimalMin(value = "0.1", message = "Interest Rate must be greater than or equal to 0.1")
    @DecimalMax(value = "0.5", message = "Interest Rate must be less than or equal to 0.5")
    private Double interestRate;
    @AllowedValue
    private Integer numberOfInstallments;
}
