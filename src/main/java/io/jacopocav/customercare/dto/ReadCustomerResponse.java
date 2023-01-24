package io.jacopocav.customercare.dto;

public record ReadCustomerResponse(
    String id,
    String firstName,
    String lastName,
    String fiscalCode,
    String address
) {
}
