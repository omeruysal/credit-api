package com.example.creditmodulechallenge.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoanResponse {
    private Long loanId;
    private Double loanAmount;
    private Integer numberOfInstallments;
    private Boolean isPaid;
}
