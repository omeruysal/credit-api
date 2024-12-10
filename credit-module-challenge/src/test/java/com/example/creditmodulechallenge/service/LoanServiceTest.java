package com.example.creditmodulechallenge.service;

import com.example.creditmodulechallenge.entity.Customer;
import com.example.creditmodulechallenge.entity.Loan;
import com.example.creditmodulechallenge.entity.LoanInstallment;
import com.example.creditmodulechallenge.exception.EligibilityException;
import com.example.creditmodulechallenge.exception.RecordNotFoundException;
import com.example.creditmodulechallenge.model.CreateLoanRequest;
import com.example.creditmodulechallenge.model.LoanResponse;
import com.example.creditmodulechallenge.model.PayLoanResponse;
import com.example.creditmodulechallenge.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class LoanServiceTest {

    @Mock
    private CustomerService customerService;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private LoanInstallmentService installmentService;

    private LoanService loanService;
    private Customer customer1;
    private Loan loan;
    private CreateLoanRequest request;
    private LoanInstallment installment1;
    private LoanInstallment installment2;
    private List<LoanInstallment> installments;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loanService = new LoanService(customerService, loanRepository, installmentService);
        customer1 = new Customer("Omer", "Uysal", "12345", 5000.0, 0.0, "ROLE_USER", "test-email");
        loan = new Loan();
        loan.setId(1L);
        loan.setLoanAmount(10000.0);
        loan.setNumberOfInstallments(12);
        loan.setIsPaid(true);
        loan.setCustomer(customer1);

        request = new CreateLoanRequest();
        request.setAmount(1000.0);
        request.setInterestRate(0.1);


        installment1 = new LoanInstallment(
                1L, loan, 5000.0, 2000.0, LocalDate.of(2025, 1, 1),
                null, true
        );

        installment2 = new LoanInstallment(
                2L, loan, 4000.0, 4000.0, LocalDate.of(2025, 2, 1),
                null, true
        );
        installments = Arrays.asList(installment1, installment2);
    }

    @Test
    void testGetLoans() {
        List<Loan> loans = Arrays.asList(loan);
        Pageable pageable = PageRequest.of(0, 10);

        when(loanRepository.findLoansByFilters(1L, 12, true, pageable)).thenReturn(new PageImpl<>(loans));


        Page<LoanResponse> result = loanService.getLoans(1L, 12, true, pageable);


        assertEquals(1, result.getContent().size());

        LoanResponse response1 = result.getContent().get(0);
        assertEquals(1L, response1.getLoanId());
        assertEquals(10000.0, response1.getLoanAmount());
        assertEquals(12, response1.getNumberOfInstallments());
        assertTrue(response1.getIsPaid());


        verify(loanRepository, times(1)).findLoansByFilters(1L, 12, true, pageable);
    }

    @Test
    void testCreatLoan_whenCustomerIsEligibilityForCredit_thenCreate() {
        when(customerService.getCustomer(request)).thenReturn(customer1);

        loanService.createLoan(request);

        verify(customerService, times(1)).getCustomer(request);
        verify(customerService, times(1)).save(customer1);
    }

    @Test
    void testCreatLoan_whenNotEligibilityForCredit_thenThrowError() {
        customer1.setCreditLimit(1000.0);

        when(customerService.getCustomer(request)).thenReturn(customer1);

        assertThrows(EligibilityException.class, () -> {
            loanService.createLoan(request);
        });
    }

    @Test
    void testPayLoan() {
        when(loanRepository.findById(anyLong())).thenReturn(Optional.of(loan));
        when(installmentService.findByLoanIdAndIsPaidFalseOrderByDueDateAsc(anyLong())).thenReturn(installments);
        when(installmentService.findByLoanIdAndIsPaidFalse(anyLong())).thenReturn(true);

        PayLoanResponse result = loanService.payLoan(loan.getId(), 10000.0);

        assertTrue(result.isLoanFullyPaid());
        assertEquals(2, result.getInstallmentsPaid());
        assertEquals(8669.0, result.getTotalAmountSpent());
    }

    @Test
    void testPayLoan_whenPenalty() {
        installments.get(0).setDueDate(LocalDate.of(2023, 1, 1));
        installments.get(1).setDueDate(LocalDate.of(2023, 2, 1));

        when(loanRepository.findById(anyLong())).thenReturn(Optional.of(loan));
        when(installmentService.findByLoanIdAndIsPaidFalseOrderByDueDateAsc(anyLong())).thenReturn(installments);
        when(installmentService.findByLoanIdAndIsPaidFalse(anyLong())).thenReturn(true);

        PayLoanResponse result = loanService.payLoan(loan.getId(), 20000.0);

        assertTrue(result.isLoanFullyPaid());
        assertEquals(2, result.getInstallmentsPaid());
        assertEquals(15248.0, result.getTotalAmountSpent());
    }

    @Test
    void testPayLoan_whenDueDateIsMoreThan3Months() {
        installments.get(0).setDueDate(LocalDate.of(2026, 1, 1));
        installments.get(1).setDueDate(LocalDate.of(2026, 2, 1));

        when(loanRepository.findById(anyLong())).thenReturn(Optional.of(loan));
        when(installmentService.findByLoanIdAndIsPaidFalseOrderByDueDateAsc(anyLong())).thenReturn(installments);
        when(installmentService.findByLoanIdAndIsPaidFalse(anyLong())).thenReturn(false);

        PayLoanResponse result = loanService.payLoan(loan.getId(), 10000.0);

        assertFalse(result.isLoanFullyPaid());
        assertEquals(0, result.getInstallmentsPaid());
        assertEquals(0, result.getTotalAmountSpent());
    }

    @Test
    void testPayLoan_whenAmountIsEnoughFor1Installment() {
        when(loanRepository.findById(anyLong())).thenReturn(Optional.of(loan));
        when(installmentService.findByLoanIdAndIsPaidFalseOrderByDueDateAsc(anyLong())).thenReturn(installments);
        when(installmentService.findByLoanIdAndIsPaidFalse(anyLong())).thenReturn(false);

        PayLoanResponse result = loanService.payLoan(loan.getId(), 5000.0);

        assertFalse(result.isLoanFullyPaid());
        assertEquals(1, result.getInstallmentsPaid());
        assertEquals(4885.0, result.getTotalAmountSpent());
    }

    @Test
    void testPayLoan_whenLoanDoesNotExist_thenThrowError() {
        when(loanRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RecordNotFoundException.class, () -> {
            loanService.payLoan(loan.getId(), 100.0);
        });
    }

}