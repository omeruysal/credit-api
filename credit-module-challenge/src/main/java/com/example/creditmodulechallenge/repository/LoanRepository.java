package com.example.creditmodulechallenge.repository;

import com.example.creditmodulechallenge.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    @Query("SELECT l FROM Loan l WHERE l.customer.id = :customerId " +
            "AND (:numberOfInstallments IS NULL OR l.numberOfInstallments = :numberOfInstallments) " +
            "AND (:isPaid IS NULL OR l.isPaid = :isPaid)")
    Page<Loan> findLoansByFilters(
            @Param("customerId") Long customerId,
            @Param("numberOfInstallments") Integer numberOfInstallments,
            @Param("isPaid") Boolean isPaid,
            Pageable pageable
    );

    Optional<Loan> findById(Long id);
}
