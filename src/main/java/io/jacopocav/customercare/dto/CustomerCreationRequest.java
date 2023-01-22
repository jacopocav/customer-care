package io.jacopocav.customercare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CustomerCreationRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9]{16}$")
    String fiscalCode,
    @NotBlank String address
) {
}
