package io.jacopocav.customercare.component;

import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.stripStart;
import static org.apache.commons.lang3.StringUtils.upperCase;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.jacopocav.customercare.dto.CreateDeviceRequest;
import io.jacopocav.customercare.dto.ReadDeviceResponse;
import io.jacopocav.customercare.dto.UpdateDeviceRequest;
import io.jacopocav.customercare.error.CustomerNotFoundException;
import io.jacopocav.customercare.model.Device;
import io.jacopocav.customercare.model.Device.Status;
import io.jacopocav.customercare.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DefaultDeviceMapper implements DeviceMapper {
    private final CustomerRepository customerRepository;

    @Override
    public ReadDeviceResponse toDto(Device entity) {
        Assert.notNull(entity, "entity is null");

        return new ReadDeviceResponse(
            entity.getId().toString(),
            entity.getStatus().toString(),
            "#" + entity.getColor(),
            entity.getCustomer().getId().toString()
        );
    }

    @Override
    public void toEntity(UpdateDeviceRequest source, Device target) {
        Assert.notNull(source, "source is null");
        Assert.notNull(target, "target is null");

        target.setColor(lowerCase(stripStart(source.color(), "#")));
        target.setStatus(Status.valueOf(upperCase(source.status())));
    }

    @Override
    public Device toNewEntity(CreateDeviceRequest request) {
        Assert.notNull(request, "request is null");

        final var customerId = UUID.fromString(request.customerId());

        final var customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

        return new Device()
            .setStatus(Status.valueOf(upperCase(request.status())))
            .setColor(lowerCase(stripStart(request.color(), "#")))
            .setCustomer(customer);
    }
}
