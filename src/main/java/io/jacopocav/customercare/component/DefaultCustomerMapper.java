package io.jacopocav.customercare.component;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.jacopocav.customercare.dto.CreateCustomerRequest;
import io.jacopocav.customercare.dto.ReadCustomerResponse;
import io.jacopocav.customercare.dto.UpdateCustomerRequest;
import io.jacopocav.customercare.model.Customer;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DefaultCustomerMapper implements CustomerMapper {
    private final DeviceMapper deviceMapper;

    @Override
    public ReadCustomerResponse toDto(Customer entity) {
        Assert.notNull(entity, "entity is null");

        final var devices = entity.getDevices().stream()
            .map(deviceMapper::toDto)
            .toList();

        return new ReadCustomerResponse(
            entity.getId().toString(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getFiscalCode(),
            entity.getAddress(),
            devices
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
