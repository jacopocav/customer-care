package io.jacopocav.customercare.component;

import io.jacopocav.customercare.dto.CreateCustomerRequest;
import io.jacopocav.customercare.dto.ReadCustomerResponse;
import io.jacopocav.customercare.dto.UpdateCustomerRequest;
import io.jacopocav.customercare.model.Customer;

public interface CustomerMapper {
    ReadCustomerResponse toDto(Customer entity);

    void toEntity(UpdateCustomerRequest source, Customer target);

    Customer toNewEntity(CreateCustomerRequest request);
}
