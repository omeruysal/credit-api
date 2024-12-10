package com.example.creditmodulechallenge.service;

import com.example.creditmodulechallenge.entity.Loan;
import com.example.creditmodulechallenge.entity.LoanInstallment;
import com.example.creditmodulechallenge.model.CreateLoanRequest;
import com.example.creditmodulechallenge.model.InstallmentResponse;
import com.example.creditmodulechallenge.model.LoanResponse;
import com.example.creditmodulechallenge.repository.LoanInstallmentRepository;
import com.example.creditmodulechallenge.repository.LoanRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LoanInstallmentService {
    private final LoanInstallmentRepository loanInstallmentRepository;

    public List<InstallmentResponse> getInstallments(Long loanId) {
        return loanInstallmentRepository.findByLoanId(loanId)
                .stream()
                .map(installment -> new InstallmentResponse(
                        installment.getAmount(),
                        installment.getPaidAmount(),
                        installment.getDueDate(),
                        installment.getPaymentDate(),
                        installment.getIsPaid()
                ))
                .collect(Collectors.toList());
    }

    public void createLoanInstallment(CreateLoanRequest request, double totalLoanAmount, Loan loan) {
        List<LoanInstallment> installments = new ArrayList<>();
        double installmentAmount = totalLoanAmount / request.getNumberOfInstallments();
        LocalDate dueDate = LocalDate.now().plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());

        for (int i = 0; i < request.getNumberOfInstallments(); i++) {
            LoanInstallment installment = LoanInstallment.builder()
                    .loan(loan)
                    .amount(installmentAmount)
                    .paidAmount(0.0)
                    .dueDate(dueDate.plusMonths(i))
                    .paymentDate(null)
                    .isPaid(false)
                    .build();
            installments.add(installment);
        }
        loanInstallmentRepository.saveAll(installments);
    }

    public boolean findByLoanIdAndIsPaidFalse(Long loanId){
        return loanInstallmentRepository.findByLoanIdAndIsPaidFalse(loanId).isEmpty();
    }

    public void saveAll(List<LoanInstallment> installments){
        loanInstallmentRepository.saveAll(installments);
    }
    public List<LoanInstallment>  findByLoanIdAndIsPaidFalseOrderByDueDateAsc(Long loanId){
       return loanInstallmentRepository
               .findByLoanIdAndIsPaidFalseOrderByDueDateAsc(loanId);
    }
}
