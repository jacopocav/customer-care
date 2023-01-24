package io.jacopocav.customercare.error;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;

public abstract class ResourceNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1;
    @Getter
    private final Serializable identifier;

    protected ResourceNotFoundException(Serializable identifier) {
        this.identifier = identifier;
    }
}
