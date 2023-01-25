package io.jacopocav.customercare.dto;

public record ReadDeviceResponse(
    String id,
    String status,
    String color,
    String customerId
) {
}
