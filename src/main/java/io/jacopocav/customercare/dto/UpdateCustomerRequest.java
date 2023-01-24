package io.jacopocav.customercare.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCustomerRequest(
    @NotBlank String address
) {
}
