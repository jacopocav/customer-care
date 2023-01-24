package io.jacopocav.customercare.service;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.jacopocav.customercare.component.DeviceMapper;
import io.jacopocav.customercare.dto.CreateDeviceRequest;
import io.jacopocav.customercare.dto.ReadDeviceResponse;
import io.jacopocav.customercare.dto.UpdateDeviceRequest;
import io.jacopocav.customercare.error.DeviceNotFoundException;
import io.jacopocav.customercare.model.Device;
import io.jacopocav.customercare.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DefaultDeviceCrudService implements DeviceCrudService {
    private final DeviceMapper mapper;
    private final DeviceRepository repository;

    @Override
    public UUID create(CreateDeviceRequest request) {
        Assert.notNull(request, "request is null");

        final Device customer = mapper.toNewEntity(request);
        return repository.save(customer).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public ReadDeviceResponse read(String id) {
        requireNotBlank(id, "id");

        final var customer = findDevice(id);
        return mapper.toDto(customer);
    }

    @Override
    public void update(String id, UpdateDeviceRequest request) {
        requireNotBlank(id, "id");
        Assert.notNull(request, "request is null");

        final var customer = findDevice(id);
        mapper.toEntity(request, customer);
    }

    @Override
    public void delete(String id) {
        requireNotBlank(id, "id");

        final var customer = findDevice(id);
        repository.delete(customer);
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
