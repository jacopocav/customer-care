package io.jacopocav.customercare.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.jacopocav.customercare.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
}
