package io.jacopocav.customercare.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.jacopocav.customercare.model.Device;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    int countByCustomerId(UUID customerId);
}
