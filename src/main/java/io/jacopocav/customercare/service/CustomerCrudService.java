package io.jacopocav.customercare.service;

import java.util.UUID;

import io.jacopocav.customercare.dto.CreateCustomerRequest;
import io.jacopocav.customercare.dto.ReadCustomerResponse;
import io.jacopocav.customercare.dto.UpdateCustomerRequest;
import jakarta.validation.constraints.NotBlank;

public interface CustomerCrudService {
    UUID create(CreateCustomerRequest request);

    ReadCustomerResponse read(@NotBlank String id);

    void update(@NotBlank String id, UpdateCustomerRequest request);

    void delete(@NotBlank String id);
}
