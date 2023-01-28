package io.jacopocav.customercare.dto;

import java.util.List;

public record ReadCustomerResponse(
    String id,
    String firstName,
    String lastName,
    String fiscalCode,
    String address,
    List<ReadDeviceResponse> devices
) {
}
