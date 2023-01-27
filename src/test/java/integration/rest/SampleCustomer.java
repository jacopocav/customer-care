package integration.rest;

import java.util.List;

import io.jacopocav.customercare.dto.CreateCustomerRequest;
import io.jacopocav.customercare.dto.ReadCustomerResponse;
import lombok.With;

@With
public record SampleCustomer(
    String id,
    String firstName,
    String lastName,
    String fiscalCode,
    String address
) {
    public static final SampleCustomer sample =
        new SampleCustomer(null, "Alice", "Bobsworth", "ALCBBS58T92C234P", "Default Road 0");

    public ReadCustomerResponse toReadResponse() {
        return new ReadCustomerResponse(id, firstName, lastName, fiscalCode, address, List.of());
    }

    public CreateCustomerRequest toCreateRequest() {
        return new CreateCustomerRequest(firstName, lastName, fiscalCode, address);
    }
}
