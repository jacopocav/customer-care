package integration.rest;

import static org.apache.commons.lang3.StringUtils.prependIfMissing;

import io.jacopocav.customercare.dto.CreateDeviceRequest;
import io.jacopocav.customercare.dto.ReadDeviceResponse;
import io.jacopocav.customercare.dto.UpdateDeviceRequest;
import lombok.With;

@With
public record SampleDevice(
    String id,
    String status,
    String color,
    String customerId
) {
    public static final SampleDevice sampleDevice =
        new SampleDevice(null, "active", "#abcdef", null);

    public ReadDeviceResponse toReadResponse() {
        return new ReadDeviceResponse(id,
            status.toUpperCase(),
            prependIfMissing(color, "#").toLowerCase(),
            customerId);
    }

    public CreateDeviceRequest toCreateRequest() {
        return new CreateDeviceRequest(customerId, status, color);
    }

    public UpdateDeviceRequest toUpdateRequest() {
        return new UpdateDeviceRequest(status, color);
    }
}
