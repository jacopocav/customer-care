package io.jacopocav.customercare.error;

import java.util.UUID;

import lombok.Getter;

public class DeviceLimitReachedException extends RuntimeException {
    @Getter
    private final int limit;

    @Getter
    private final UUID customerId;

    public DeviceLimitReachedException(int limit, UUID customerId) {
        super("customer %s already has the maximum allowed number of devices (%d)"
            .formatted(customerId, limit));
        this.limit = limit;
        this.customerId = customerId;
    }
}
