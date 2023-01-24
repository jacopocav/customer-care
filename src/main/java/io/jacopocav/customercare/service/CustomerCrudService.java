package io.jacopocav.customercare.service;

import java.util.UUID;

import io.jacopocav.customercare.dto.CreateCustomerRequest;
import io.jacopocav.customercare.dto.ReadCustomerResponse;
import io.jacopocav.customercare.dto.UpdateCustomerRequest;

public interface CustomerCrudService {
    UUID create(CreateCustomerRequest request);

    ReadCustomerResponse read(String id);

    void update(String id, UpdateCustomerRequest request);
}
