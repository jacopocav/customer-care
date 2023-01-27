package integration.rest;

import java.util.List;

import io.jacopocav.customercare.dto.CreateCustomerRequest;
import io.jacopocav.customercare.dto.ReadCustomerResponse;
import io.jacopocav.customercare.dto.UpdateCustomerRequest;
import lombok.With;

@With
public record SampleCustomer(
    String id,
    String firstName,
    String lastName,
    String fiscalCode,
    String address,
    List<SampleDevice> devices
) {
    public static final SampleCustomer sampleCustomer =
        new SampleCustomer(null, "Alice", "Bobsworth", "ALCBBS58T92C234P", "Default Road 0",
            List.of());

    public ReadCustomerResponse toReadResponse() {
        return new ReadCustomerResponse(id, firstName, lastName, fiscalCode, address,
            devices.stream()
                .map(SampleDevice::toReadResponse)
                .toList());
    }

    public CreateCustomerRequest toCreateRequest() {
        return new CreateCustomerRequest(firstName, lastName, fiscalCode, address);
    }

    public UpdateCustomerRequest toUpdateRequest() {
        return new UpdateCustomerRequest(address);
    }
}
