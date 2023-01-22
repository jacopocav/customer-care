package io.jacopocav.customercare.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerUpdateRequest(
    @NotBlank String address
) {
}
