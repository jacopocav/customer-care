package io.jacopocav.customercare.dto;

import static jakarta.validation.constraints.Pattern.Flag.CASE_INSENSITIVE;

import jakarta.validation.constraints.Pattern;

public record UpdateDeviceRequest(
    @Pattern(regexp = "^(ACTIVE|INACTIVE|LOST)$", flags = CASE_INSENSITIVE)
    String status,
    @Pattern(regexp = "^#?[0-9a-f]{6}$", flags = CASE_INSENSITIVE)
    String color
) {
}
