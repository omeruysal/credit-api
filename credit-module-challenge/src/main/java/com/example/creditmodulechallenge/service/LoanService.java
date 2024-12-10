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
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class LoanService {
    public static final String CUSTOMER_DOES_NOT_HAVE_ENOUGH_CREDIT_LIMIT = "Customer does not have enough credit limit.";
    public static final double ADJUSTMENT_RATE = 0.001;
    public static final String LOAN_NOT_FOUND = "Loan not found. Loan Id : ";
    public static final String MONTHS_CANNOT_BE_PAID = "Installments have due date that still more than 3 calendar months cannot be paid";
    public static final String THE_SENT_AMOUNT_IS_LESS_THAN_INSTALLMENT_AMOUNT = "The sent amount is less than installment amount.";
    public static final String THE_SENT_AMOUNT_IS_FINISHED = "The sent amount is finished";
    public static final String CREATING_LOAN = "Loan is creating. Customer Id : ";
    public static final String PAYING_LOAN_CUSTOMER_ID = "Customer is paying loan. Customer Id : ";
    public static final String PAYMENT_IS_MADE = "Payment is made for installment. Installment Id : ";
    public static final String LOAN_IS_FULLY_PAID = "Loan is paid fully. Loan Id : ";
    private final CustomerService customerService;
    private final LoanRepository loanRepository;
    private final LoanInstallmentService installmentService;

    public Page<LoanResponse> getLoans(Long customerId, Integer numberOfInstallments, Boolean isPaid, Pageable pageable) {
        Page<Loan> loans = loanRepository.findLoansByFilters(customerId, numberOfInstallments, isPaid, pageable);

        return loans.map(loan -> new LoanResponse(
                loan.getId(),
                loan.getLoanAmount(),
                loan.getNumberOfInstallments(),
                loan.getIsPaid()
        ));
    }

    @Transactional //wraps all the process to one single transaction
    public void createLoan(CreateLoanRequest request) {
        log.info(CREATING_LOAN + request.getCustomerId());

        Customer customer = customerService.getCustomer(request);

        // Check credit score
        double totalLoanAmount = creditEligibility(request, customer);

        // Create Loan
        Loan loan = createLoan(request, customer, totalLoanAmount);

        // Create Installment
        installmentService.createLoanInstallment(request, totalLoanAmount, loan);

        // Update customer credit limit
        customer.setUsedCreditLimit(customer.getUsedCreditLimit() + totalLoanAmount);
        customerService.save(customer);
    }

    @Transactional //wraps all the process to one single transaction
    public PayLoanResponse payLoan(Long loanId, Double amount) {
        Loan loan = getLoan(loanId);

        log.info(PAYING_LOAN_CUSTOMER_ID + loan.getCustomer().getId());

        List<LoanInstallment> installments = installmentService
                .findByLoanIdAndIsPaidFalseOrderByDueDateAsc(loanId);

        double totalAmountSpent = 0;
        int installmentsPaid = 0;
        double creditLimit = 0;
        LocalDate today = LocalDate.now();
        List<LoanInstallment> paidInstallments = new ArrayList<>();

        for (LoanInstallment installment : installments) {
            // Installments have due date that still more than 3 calendar months cannot be paid
            if (ChronoUnit.DAYS.between(today, installment.getDueDate()) > 90) {
                log.warn(MONTHS_CANNOT_BE_PAID);
                break;
            }

            // Get amount with reward or penalty
            double adjustedAmount = getAdjustedAmount(today, installment);

            // Check sent amount is less than adjustedAmount
            if (amount < adjustedAmount) {
                log.warn(THE_SENT_AMOUNT_IS_LESS_THAN_INSTALLMENT_AMOUNT);
                break;
            }

            // Payment operation
            amount -= adjustedAmount;
            totalAmountSpent += adjustedAmount;
            installmentsPaid++;
            installment.setPaidAmount(adjustedAmount);
            installment.setPaymentDate(today);
            installment.setIsPaid(true);
            paidInstallments.add(installment);

            // Adjust credit limit
            creditLimit += installment.getAmount();

            // When the sent amount is spent
            if (amount <= 0) {
                log.warn(THE_SENT_AMOUNT_IS_FINISHED);
                break;
            }
        }
        installmentService.saveAll(paidInstallments);

        paidInstallments.forEach(i -> log.info(PAYMENT_IS_MADE + i.getId()));

        boolean isLoanFullyPaid = isLoanFullyPaid(loanId, loan);

        updateCustomerCreditLimit(loan, creditLimit);

        return new PayLoanResponse(installmentsPaid, totalAmountSpent, isLoanFullyPaid);
    }

    private void updateCustomerCreditLimit(Loan loan, double creditLimit) {
        Customer customer = loan.getCustomer();
        customer.setUsedCreditLimit(customer.getUsedCreditLimit() - creditLimit);
        customerService.save(customer);
    }

    private boolean isLoanFullyPaid(Long loanId, Loan loan) {
        boolean isLoanFullyPaid = installmentService.findByLoanIdAndIsPaidFalse(loanId);
        if (isLoanFullyPaid) {
            log.info(LOAN_IS_FULLY_PAID + loan.getId());
            loan.setIsPaid(true);
            loanRepository.save(loan);
        }
        return isLoanFullyPaid;
    }

    private static double getAdjustedAmount(LocalDate today, LoanInstallment installment) {
        double adjustedAmount = installment.getAmount();
        if (today.isBefore(installment.getDueDate())) {
            long daysBeforeDueDate = ChronoUnit.DAYS.between(today, installment.getDueDate());
            adjustedAmount -= adjustedAmount * ADJUSTMENT_RATE * daysBeforeDueDate;
        } else if (today.isAfter(installment.getDueDate())) {
            long daysAfterDueDate = ChronoUnit.DAYS.between(installment.getDueDate(), today);
            adjustedAmount += adjustedAmount * ADJUSTMENT_RATE * daysAfterDueDate;
        }
        return adjustedAmount;
    }

    private double creditEligibility(CreateLoanRequest request, Customer customer) {
        double totalLoanAmount = request.getAmount() * (1 + request.getInterestRate());
        if (customer.getCreditLimit() - customer.getUsedCreditLimit() < totalLoanAmount) {
            log.error(CUSTOMER_DOES_NOT_HAVE_ENOUGH_CREDIT_LIMIT);
            throw new EligibilityException(CUSTOMER_DOES_NOT_HAVE_ENOUGH_CREDIT_LIMIT);
        }
        return totalLoanAmount;
    }

    private Loan createLoan(CreateLoanRequest request, Customer customer, double totalLoanAmount) {
        Loan loan = Loan.builder()
                .customer(customer)
                .loanAmount(totalLoanAmount)
                .numberOfInstallments(request.getNumberOfInstallments())
                .createDate(LocalDate.now())
                .isPaid(false)
                .build();
        return loanRepository.save(loan);
    }


    public Loan getLoan(Long loanId) {
        Optional<Loan> loanOptional = loanRepository.findById(loanId);
        if (loanOptional.isEmpty()) {
            log.error(LOAN_NOT_FOUND + loanId);
            throw new RecordNotFoundException(LOAN_NOT_FOUND + loanId);
        }
        return loanOptional.get();
    }
}
