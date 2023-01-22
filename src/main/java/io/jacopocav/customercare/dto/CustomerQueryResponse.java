package io.jacopocav.customercare.dto;

public record CustomerQueryResponse(
    String id,
    String firstName,
    String lastName,
    String fiscalCode,
    String address
) {
}
