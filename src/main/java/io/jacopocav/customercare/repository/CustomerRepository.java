package io.jacopocav.customercare.repository;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import io.jacopocav.customercare.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    @EntityGraph(attributePaths = Customer.Fields.devices, type = LOAD)
    Optional<Customer> findFetchingDevicesById(UUID id);
}
