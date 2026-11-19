package com.intbank.service;

import com.intbank.domain.Customer;
import com.intbank.domain.DomainExceptions;
import com.intbank.domain.User;
import com.intbank.repository.CustomerRepository;
import com.intbank.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Memetakan principal Spring Security ke User/Customer di database. */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public CurrentUserService(UserRepository userRepository, CustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new DomainExceptions.AccountAccessDeniedException();
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(DomainExceptions.AccountAccessDeniedException::new);
    }

    public Customer currentCustomer() {
        return customerRepository.findByUserId(currentUser().getId())
                .orElseThrow(DomainExceptions.AccountAccessDeniedException::new);
    }

    public Long currentUserId() {
        return currentUser().getId();
    }

    public Long currentCustomerId() {
        return currentCustomer().getId();
    }

    @org.springframework.transaction.annotation.Transactional
    public void updateProfile(String fullName, String phone, String address) {
        Customer customer = currentCustomer();
        customer.updateProfile(fullName, phone, address);
        customerRepository.save(customer);
    }
}
