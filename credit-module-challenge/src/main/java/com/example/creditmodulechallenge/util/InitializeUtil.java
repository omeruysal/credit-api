package com.example.creditmodulechallenge.util;

import com.example.creditmodulechallenge.entity.Customer;
import com.example.creditmodulechallenge.repository.CustomerRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;

@Component
@AllArgsConstructor
public class InitializeUtil {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        Customer customer1 = new Customer("Omer", "Uysal", passwordEncoder.encode("12345"), 1000.0, 0.0, "ROLE_ADMIN","omer@outlook.com");
        Customer customer2 = new Customer("Ali", "Uysal", passwordEncoder.encode("12345"), 1000.0, 0.0, "ROLE_USER","ali@outlook.com");
        Customer customer3 = new Customer("Veli", "Uysal", passwordEncoder.encode("12345"), 1000.0, 0.0, "ROLE_USER","veli@outlook.com");
        customerRepository.saveAll(asList(customer1, customer2, customer3));
    }
}
