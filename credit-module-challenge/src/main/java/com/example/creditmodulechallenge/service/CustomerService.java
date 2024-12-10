package com.example.creditmodulechallenge.service;

import com.example.creditmodulechallenge.entity.Customer;
import com.example.creditmodulechallenge.exception.RecordNotFoundException;
import com.example.creditmodulechallenge.model.CreateLoanRequest;
import com.example.creditmodulechallenge.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class CustomerService {
    public static final String NOT_FOUND_CUSTOMER_ID = "Customer not found. Customer Id : ";
    private final CustomerRepository customerRepository;

    public Customer getCustomer(CreateLoanRequest request) {
        Optional<Customer> customerOptional = customerRepository.findById(request.getCustomerId());
        if (customerOptional.isEmpty()) {
            log.error(NOT_FOUND_CUSTOMER_ID + request.getCustomerId());
            throw new RecordNotFoundException(NOT_FOUND_CUSTOMER_ID + request.getCustomerId());

        }
        return customerOptional.get();
    }

    public Customer save(Customer customer){
        return customerRepository.save(customer);
    }


    public Customer getAuthenticatedUser() {
        return (Customer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email).orElseThrow();
    }
}
