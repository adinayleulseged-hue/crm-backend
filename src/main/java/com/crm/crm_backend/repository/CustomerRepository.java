package com.crm.crm_backend.repository;

// repository/CustomerRepository.java

import com.crm.crm_backend.model.entity.Customer;
import com.crm.crm_backend.model.enums.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>,
        JpaSpecificationExecutor<Customer> {
    long count();
    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByCustomerNumber(String customerNumber);

    List<Customer> findByStatus(CustomerStatus status);

    List<Customer> findByCompanyNameContainingIgnoreCase(String companyName);

    boolean existsByEmail(String email);

    boolean existsByCustomerNumber(String customerNumber);
}