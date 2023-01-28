package io.jacopocav.customercare.error;

import java.io.Serial;
import java.util.UUID;

public class CustomerNotFoundException extends ResourceNotFoundException {
    @Serial
    private static final long serialVersionUID = 1;

    public CustomerNotFoundException(UUID id) {
        super(id);
    }
}
