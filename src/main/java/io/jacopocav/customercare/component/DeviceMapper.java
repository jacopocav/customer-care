package io.jacopocav.customercare.component;

import io.jacopocav.customercare.dto.CreateDeviceRequest;
import io.jacopocav.customercare.dto.ReadDeviceResponse;
import io.jacopocav.customercare.dto.UpdateDeviceRequest;
import io.jacopocav.customercare.model.Device;

public interface DeviceMapper {
    ReadDeviceResponse toDto(Device entity);

    void toEntity(UpdateDeviceRequest source, Device target);

    Device toNewEntity(CreateDeviceRequest request);
}
