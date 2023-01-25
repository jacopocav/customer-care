package io.jacopocav.customercare.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.jacopocav.customercare.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    @Query("select c "
        + "from Customer c "
        + "join fetch c.devices "
        + "where c.id = :id")
    Optional<Customer> findByIdFetchingDevices(UUID id);
}
