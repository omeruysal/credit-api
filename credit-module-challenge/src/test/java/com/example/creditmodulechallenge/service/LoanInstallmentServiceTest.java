package com.example.creditmodulechallenge.service;

import com.example.creditmodulechallenge.entity.Loan;
import com.example.creditmodulechallenge.entity.LoanInstallment;
import com.example.creditmodulechallenge.model.CreateLoanRequest;
import com.example.creditmodulechallenge.model.InstallmentResponse;
import com.example.creditmodulechallenge.repository.LoanInstallmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoanInstallmentServiceTest {
    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    private LoanInstallmentService loanService;
    Loan loan;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loanService = new LoanInstallmentService(loanInstallmentRepository);
        loan = new Loan();
        loan.setId(1L);
    }

    @Test
    void testGetInstallments_whenGivenExistsLoanId_thenReturnInstallments() {
        LoanInstallment installment1 = new LoanInstallment(
                1L, loan, 5000.0, 2000.0, LocalDate.of(2025, 1,1 ),
                LocalDate.of(2024, 12, 8), true
        );

        LoanInstallment installment2 = new LoanInstallment(
                2L, loan, 4000.0, 4000.0, LocalDate.of(2025, 2, 1),
                LocalDate.of(2024, 12, 8), true
        );
        List<LoanInstallment> installments = Arrays.asList(installment1, installment2);

        when(loanInstallmentRepository.findByLoanId(1L)).thenReturn(installments);


        List<InstallmentResponse> result = loanService.getInstallments(1L);


        assertThat(result).hasSize(2);

        InstallmentResponse response1 = result.get(0);
        assertEquals(installment1.getAmount(), response1.getAmount());
        assertEquals(installment1.getPaidAmount(), response1.getPaidAmount());
        assertEquals(installment1.getDueDate(), response1.getDueDate());
        assertEquals(installment1.getPaymentDate(), response1.getPaymentDate());
        assertEquals(installment1.getIsPaid(), response1.getIsPaid());

        InstallmentResponse response2 = result.get(1);
        assertEquals(installment2.getAmount(), response2.getAmount());
        assertEquals(installment2.getPaidAmount(), response2.getPaidAmount());
        assertEquals(installment2.getDueDate(), response2.getDueDate());
        assertEquals(installment2.getPaymentDate(), response2.getPaymentDate());
        assertEquals(installment2.getIsPaid(), response2.getIsPaid());

        verify(loanInstallmentRepository, times(1)).findByLoanId(1L);
    }

    @Test
    void testCreateLoanInstallment_whenGivenValidRequest_thenCreate() {
        CreateLoanRequest request = new CreateLoanRequest();
        request.setNumberOfInstallments(9);
        double totalLoanAmount = 9000.0;

        loanService.createLoanInstallment(request, totalLoanAmount, loan);

        verify(loanInstallmentRepository, times(1)).saveAll(anyList());

        ArgumentCaptor<List<LoanInstallment>> captor = ArgumentCaptor.forClass(List.class);
        verify(loanInstallmentRepository).saveAll(captor.capture());

        List<LoanInstallment> savedInstallments = captor.getValue();
        assertEquals(9, savedInstallments.size());


        LoanInstallment firstInstallment = savedInstallments.get(0);
        assertEquals(totalLoanAmount / 9, firstInstallment.getAmount());
        assertEquals(0.0, firstInstallment.getPaidAmount());
        assertNull(firstInstallment.getPaymentDate());
        assertFalse(firstInstallment.getIsPaid());
   }
}
