package io.jacopocav.customercare.service;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.jacopocav.customercare.component.DeviceMapper;
import io.jacopocav.customercare.dto.CreateDeviceRequest;
import io.jacopocav.customercare.dto.ReadDeviceResponse;
import io.jacopocav.customercare.dto.UpdateDeviceRequest;
import io.jacopocav.customercare.error.DeviceLimitReachedException;
import io.jacopocav.customercare.error.DeviceNotFoundException;
import io.jacopocav.customercare.model.Device;
import io.jacopocav.customercare.repository.DeviceRepository;

@Service
@Transactional
public class DefaultDeviceCrudService implements DeviceCrudService {
    private final DeviceMapper mapper;
    private final DeviceRepository repository;
    private final int maxDevicesPerCustomer;

    public DefaultDeviceCrudService(
        DeviceMapper mapper,
        DeviceRepository repository,
        @Value("${customer-care.max-devices-per-customer}") int maxDevicesPerCustomer
    ) {
        this.mapper = mapper;
        this.repository = repository;
        this.maxDevicesPerCustomer = maxDevicesPerCustomer;
    }

    @Override
    public UUID create(CreateDeviceRequest request) {
        Assert.notNull(request, "request is null");

        final var customerId = UUID.fromString(request.customerId());
        final var deviceCount = repository.countByCustomerId(customerId);

        if (deviceCount >= maxDevicesPerCustomer) {
            throw new DeviceLimitReachedException(maxDevicesPerCustomer, customerId);
        }

        final Device device = mapper.toNewEntity(request);

        return repository.save(device).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public ReadDeviceResponse read(String id) {
        requireNotBlank(id, "id");

        final var device = findDevice(id);
        return mapper.toDto(device);
    }

    @Override
    public void update(String id, UpdateDeviceRequest request) {
        requireNotBlank(id, "id");
        Assert.notNull(request, "request is null");

        final var device = findDevice(id);
        mapper.toEntity(request, device);
    }

    @Override
    public void delete(String id) {
        requireNotBlank(id, "id");

        final var device = findDevice(id);
        repository.delete(device);
    }

    private Device findDevice(String id) {
        final var uuid = UUID.fromString(id);
        return repository.findById(uuid)
            .orElseThrow(() -> new DeviceNotFoundException(uuid));
    }

    private static void requireNotBlank(String value, String name) {
        Assert.isTrue(isNotBlank(value), name + " is blank or null");
    }
}
