package io.jacopocav.customercare.component;

import io.jacopocav.customercare.dto.CustomerQueryResponse;
import io.jacopocav.customercare.dto.CustomerUpdateRequest;
import io.jacopocav.customercare.model.Customer;

public interface CustomerMapper {
    CustomerQueryResponse toDto(Customer entity);

    void toEntity(CustomerUpdateRequest source, Customer target);
}
