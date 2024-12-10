package com.example.creditmodulechallenge.repository;

import com.example.creditmodulechallenge.entity.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {
    List<LoanInstallment> findByLoanId(Long loanId);
    List<LoanInstallment> findByLoanIdAndIsPaidFalseOrderByDueDateAsc(Long loanId);
    List<LoanInstallment> findByLoanIdAndIsPaidFalse(Long loanId);

}
