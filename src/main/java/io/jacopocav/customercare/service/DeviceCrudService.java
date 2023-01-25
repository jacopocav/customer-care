package io.jacopocav.customercare.service;

import java.util.UUID;

import io.jacopocav.customercare.dto.CreateDeviceRequest;
import io.jacopocav.customercare.dto.ReadDeviceResponse;
import io.jacopocav.customercare.dto.UpdateDeviceRequest;
import jakarta.validation.constraints.NotBlank;

public interface DeviceCrudService {
    UUID create(CreateDeviceRequest request);

    ReadDeviceResponse read(@NotBlank String id);

    void update(@NotBlank String id, UpdateDeviceRequest request);

    void delete(@NotBlank String id);

    boolean exists(@NotBlank String id);
}
