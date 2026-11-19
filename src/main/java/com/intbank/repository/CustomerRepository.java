package com.intbank.repository;

import com.intbank.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUserId(Long userId);

    @Query("select c from Customer c, User u where u.id = c.userId " +
            "and (lower(c.fullName) like lower(concat('%', :keyword, '%')) " +
            "or lower(u.email) like lower(concat('%', :keyword, '%')))")
    List<Customer> searchByNameOrEmail(@Param("keyword") String keyword);
}
