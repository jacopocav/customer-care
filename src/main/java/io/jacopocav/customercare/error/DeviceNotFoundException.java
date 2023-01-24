package io.jacopocav.customercare.error;

import java.io.Serial;
import java.util.UUID;

public class DeviceNotFoundException extends ResourceNotFoundException {
    @Serial
    private static final long serialVersionUID = 1;

    public DeviceNotFoundException(UUID id) {
        super(id);
    }
}
