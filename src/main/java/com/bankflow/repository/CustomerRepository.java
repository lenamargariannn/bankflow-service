package com.bankflow.repository;

import com.bankflow.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUser_Email(String email);

    Optional<Customer> findByUser_Username(String username);

    Optional<Customer> findByPhoneNumber(String phoneNumber);
}

