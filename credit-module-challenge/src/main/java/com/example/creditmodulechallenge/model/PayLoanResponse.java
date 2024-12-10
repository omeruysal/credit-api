package com.example.creditmodulechallenge.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PayLoanResponse {
    private int installmentsPaid;
    private double totalAmountSpent;
    private boolean isLoanFullyPaid;
}