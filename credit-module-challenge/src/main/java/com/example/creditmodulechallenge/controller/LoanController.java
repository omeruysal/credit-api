package com.example.creditmodulechallenge.controller;

import com.example.creditmodulechallenge.model.CreateLoanRequest;
import com.example.creditmodulechallenge.model.InstallmentResponse;
import com.example.creditmodulechallenge.model.LoanResponse;
import com.example.creditmodulechallenge.model.PayLoanRequest;
import com.example.creditmodulechallenge.model.PayLoanResponse;
import com.example.creditmodulechallenge.service.AuthenticationService;
import com.example.creditmodulechallenge.service.LoanInstallmentService;
import com.example.creditmodulechallenge.service.LoanService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/loans")
@AllArgsConstructor
public class LoanController {

    public static final String LOAN_CREATED_SUCCESSFULLY = "Loan created successfully.";
    private final LoanService loanService;
    private final LoanInstallmentService installmentService;
    private final AuthenticationService authenticationService;

    @GetMapping("")
    public ResponseEntity<Page<LoanResponse>> getLoans(
            @RequestParam Long customerId,
            @RequestParam(required = false) Integer numberOfInstallments,
            @RequestParam(required = false) Boolean isPaid,
            Pageable pageable) {

        authenticationService.checkAuthorization(customerId);
        Page<LoanResponse> loans = loanService.getLoans(customerId, numberOfInstallments, isPaid, pageable);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/{loanId}/installments")
    public ResponseEntity<List<InstallmentResponse>> getLoanInstallments(@PathVariable Long loanId) {
        authenticationService.checkAuthorizationWithLoanId(loanId);
        List<InstallmentResponse> installments = installmentService.getInstallments(loanId);
        return ResponseEntity.ok(installments);
    }

    @PostMapping()
    public ResponseEntity<String> createLoan(@RequestBody @Valid CreateLoanRequest request) {
        authenticationService.checkAuthorization(request.getCustomerId());
        loanService.createLoan(request);
        return ResponseEntity.ok(LOAN_CREATED_SUCCESSFULLY);

    }

    @PostMapping("/{loanId}/pay")
    public ResponseEntity<PayLoanResponse> payLoan(
            @PathVariable Long loanId, @RequestBody @Valid PayLoanRequest request) {
        authenticationService.checkAuthorizationWithLoanId(loanId);
        PayLoanResponse response = loanService.payLoan(loanId, request.getAmount());
        return ResponseEntity.ok(response);
    }

}
