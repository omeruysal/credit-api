package com.example.creditmodulechallenge.service;

import com.example.creditmodulechallenge.entity.Customer;
import com.example.creditmodulechallenge.exception.RecordNotFoundException;
import com.example.creditmodulechallenge.model.CreateLoanRequest;
import com.example.creditmodulechallenge.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @InjectMocks
    private CustomerService customerService;
    private Customer customer1;
    private final Long customerId = 1L;

    private CreateLoanRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customerService = new CustomerService(customerRepository);
        request = new CreateLoanRequest();
        customer1 = new Customer("Omer", "Uysal", "12345", 1000.0, 0.0, "ROLE_USER","test-email");
    }

    @Test
    void testGetCustomer_whenGivenExistsCustomerId_thenReturnCustomer() {
        request.setCustomerId(1L);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer1));

        Customer result = customerService.getCustomer(request);

        assertEquals(customer1.getId(), result.getId());
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void testGetCustomer_whenGivenNotExistCustomerId_thenReturnCustomer() {
        request.setCustomerId(2L);

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());


        assertThrows(RecordNotFoundException.class, () -> {
            customerService.getCustomer(request);
        });
    }
}