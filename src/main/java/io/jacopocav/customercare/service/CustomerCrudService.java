package io.jacopocav.customercare.service;

import java.util.UUID;

import io.jacopocav.customercare.dto.CustomerCreationRequest;
import io.jacopocav.customercare.dto.CustomerQueryResponse;
import io.jacopocav.customercare.dto.CustomerUpdateRequest;

public interface CustomerCrudService {
    UUID create(CustomerCreationRequest request);

    CustomerQueryResponse read(String id);

    void update(CustomerUpdateRequest request);
}
