package io.jacopocav.customercare.component;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.jacopocav.customercare.dto.CreateCustomerRequest;
import io.jacopocav.customercare.dto.ReadCustomerResponse;
import io.jacopocav.customercare.dto.UpdateCustomerRequest;
import io.jacopocav.customercare.model.Customer;

@Component
public class DefaultCustomerMapper implements CustomerMapper {
    @Override
    public ReadCustomerResponse toDto(Customer entity) {
        Assert.notNull(entity, "entity is null");

        return new ReadCustomerResponse(
            entity.getId().toString(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getFiscalCode(),
            entity.getAddress(),
            List.of()
        );
    }

    @Override
    public void toEntity(UpdateCustomerRequest source, Customer target) {
        Assert.notNull(source, "source is null");
        Assert.notNull(target, "target is null");

        target.setAddress(source.address());
    }

    @Override
    public Customer toNewEntity(CreateCustomerRequest request) {
        Assert.notNull(request, "request is null");

        return new Customer()
            .setFirstName(request.firstName())
            .setLastName(request.lastName())
            .setFiscalCode(request.fiscalCode())
            .setAddress(request.address());
    }
}
