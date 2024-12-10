package com.example.creditmodulechallenge.service;

import com.example.creditmodulechallenge.entity.Customer;
import com.example.creditmodulechallenge.entity.Loan;
import com.example.creditmodulechallenge.exception.CustomAccessDeniedException;
import com.example.creditmodulechallenge.model.AuthenticationRequest;
import com.example.creditmodulechallenge.model.AuthenticationResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final CustomerService customerService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LoanService loanService;


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        Customer customer = customerService.findByEmail(request.getEmail());
        String jwtToken = jwtService.generateToken(customer);

        return  new AuthenticationResponse(jwtToken);
    }

    public void checkAuthorization(Long customerId){
       Customer customer = customerService.getAuthenticatedUser();
       if(!customer.getRole().equals("ROLE_ADMIN") && !customer.getId().equals(customerId)){
           throw new CustomAccessDeniedException("Authorization error");
       }
    }
    public void checkAuthorizationWithLoanId(Long loanId){
        Customer customer = customerService.getAuthenticatedUser();
        if(!customer.getRole().equals("ROLE_ADMIN")){
            Loan loan = loanService.getLoan(loanId);
            if(!loan.getCustomer().getId().equals(customer.getId())){
                throw new CustomAccessDeniedException("Authorization error");
            }
        }
    }
}
