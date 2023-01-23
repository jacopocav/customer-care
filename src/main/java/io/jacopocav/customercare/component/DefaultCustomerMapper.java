package io.jacopocav.customercare.component;

import org.springframework.util.Assert;

import io.jacopocav.customercare.dto.CustomerQueryResponse;
import io.jacopocav.customercare.dto.CustomerUpdateRequest;
import io.jacopocav.customercare.model.Customer;

public class DefaultCustomerMapper implements CustomerMapper {
    @Override
    public CustomerQueryResponse toDto(Customer entity) {
        Assert.notNull(entity, "entity is null");

        return new CustomerQueryResponse(
            entity.getId().toString(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getFiscalCode(),
            entity.getAddress()
        );
    }

    @Override
    public void toEntity(CustomerUpdateRequest source, Customer target) {
        Assert.notNull(source, "source is null");
        Assert.notNull(target, "target is null");

        target.setAddress(source.address());
    }
}
