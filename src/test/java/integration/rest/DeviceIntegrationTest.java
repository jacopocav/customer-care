package integration.rest;

import static integration.rest.DeviceIntegrationTest.deviceLimit;
import static integration.rest.SampleCustomer.sampleCustomer;
import static integration.rest.SampleDevice.sampleDevice;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import integration.RestIntegrationTest;
import io.jacopocav.customercare.CustomerCareApplication;
import io.jacopocav.customercare.dto.ErrorResponse;
import io.jacopocav.customercare.dto.ReadCustomerResponse;
import io.jacopocav.customercare.dto.ReadDeviceResponse;
import lombok.Getter;

@ActiveProfiles("test")
@SpringBootTest(
    classes = CustomerCareApplication.class,
    webEnvironment = RANDOM_PORT,
    properties = "customer-care.max-devices-per-customer=" + deviceLimit
)
class DeviceIntegrationTest implements RestIntegrationTest {
    public static final int deviceLimit = 3;
    @Getter
    @LocalServerPort
    int port;

    @Getter
    @Autowired
    TestRestTemplate rest;

    SampleDevice device;
    SampleCustomer owner;

    @BeforeEach
    void setUp() {
        owner = prepareSampleCustomer();
        device = sampleDevice.withCustomerId(owner.id());
    }

    @Test
    void create() {
        // create device
        final var createResponse =
            rest.postForEntity(urlOf("/devices"), device.toCreateRequest(), String.class);

        then(createResponse.getStatusCode())
            .isEqualTo(CREATED);
        then(createResponse.getHeaders().getLocation())
            .isNotNull();

        final var deviceId = getIdFromLocation(createResponse.getHeaders().getLocation());

        // read created device
        final var readResponse = readDevice(deviceId.toString());
        final var expectedDevice = device
            .withId(deviceId.toString())
            .toReadResponse();

        then(readResponse)
            .isEqualTo(expectedDevice);

        // read created device from customer
        final var readCustomerResponse = readCustomer(owner.id());
        final var expectedCustomer = sampleCustomer
            .withId(owner.id())
            .withDevices(List.of(device.withId(deviceId.toString())))
            .toReadResponse();

        then(readCustomerResponse)
            .isEqualTo(expectedCustomer);
    }

    @Test
    void create_fails_givenLimitReached() {
        // prepare the maximum number of devices
        range(0, deviceLimit).forEach(i -> addSampleDevice());

        // create another device
        final var createResponse =
            rest.postForEntity(urlOf("/devices"), device.toCreateRequest(), ErrorResponse.class);

        then(createResponse.getStatusCode())
            .isEqualTo(BAD_REQUEST);

        final var error = createResponse.getBody();

        then(error)
            .isNotNull();
        then(error.summary())
            .isEqualToIgnoringCase("device limit reached");
        then(error.description())
            .containsIgnoringCase(owner.id());
    }

    @Test
    void read_singleDevice() {
        // prepare device
        prepareSampleDevice();

        // read non-existing device
        final var notFoundResponse =
            rest.getForEntity(urlOf("/devices/" + UUID.randomUUID()), ReadDeviceResponse.class);

        then(notFoundResponse.getStatusCode())
            .isEqualTo(NOT_FOUND);

        // read existing device
        final var readResponse =
            rest.getForEntity(urlOf("/devices/" + device.id()), ReadDeviceResponse.class);

        then(readResponse.getStatusCode())
            .isEqualTo(OK);
        then(readResponse.getBody())
            .isEqualTo(device.toReadResponse());
    }

    @Test
    void read_fromCustomer() {
        // prepare device
        prepareSampleDevice();
        final var unrelatedCustomer = prepareSampleCustomer();

        // read from unrelated customer
        final var wrongCustomerResponse =
            rest.getForEntity(urlOf("/customers/" + unrelatedCustomer.id()),
                ReadCustomerResponse.class);

        then(wrongCustomerResponse.getStatusCode())
            .isEqualTo(OK);
        then(wrongCustomerResponse.getBody())
            .isNotNull();
        then(wrongCustomerResponse.getBody().devices())
            .isNullOrEmpty();

        // read existing device
        final var readResponse =
            rest.getForEntity(urlOf("/customers/" + device.customerId()),
                ReadCustomerResponse.class);
        final var expected = sampleCustomer
            .withId(device.customerId())
            .withDevices(List.of(device))
            .toReadResponse();

        then(readResponse.getStatusCode())
            .isEqualTo(OK);
        then(readResponse.getBody())
            .isEqualTo(expected);
    }

    @Test
    void update() {
        prepareSampleDevice();

        // update device
        final var sampleAfterUpdate = device
            .withStatus("lost")
            .withColor("AABBCC");

        final var updateRequest = sampleAfterUpdate.toUpdateRequest();

        final var updateResponse = patchForEntity(urlOf("/devices/" + device.id()),
            updateRequest, String.class);

        then(updateResponse.getStatusCode())
            .isEqualTo(OK);

        // read updated device
        final var readResponse = readDevice(device.id());
        final var expected = sampleAfterUpdate.toReadResponse();

        then(readResponse)
            .isEqualTo(expected);

        // read updated device through customer
        final var customerResponse = readCustomer(owner.id());
        final var expectedCustomer = sampleCustomer
            .withId(owner.id())
            .withDevices(List.of(sampleAfterUpdate))
            .toReadResponse();

        then(customerResponse)
            .isEqualTo(expectedCustomer);
    }

    @Test
    void delete() {
        prepareSampleDevice();

        // delete device
        final var deleteResponse =
            deleteForEntity(urlOf("/devices/" + device.id()), String.class);

        then(deleteResponse.getStatusCode())
            .isEqualTo(OK);

        // read deleted device
        final var readResponse =
            rest.getForEntity(urlOf("/devices" + device.id()), ReadDeviceResponse.class);

        then(readResponse.getStatusCode())
            .isEqualTo(NOT_FOUND);

        // read deleted device through customer
        final var readCustomer = readCustomer(owner.id());
        final var expected = sampleCustomer
            .withId(owner.id())
            .withDevices(List.of())
            .toReadResponse();

        then(readCustomer)
            .isEqualTo(expected);

        // delete device again
        final var deleteAgainResponse =
            deleteForEntity(urlOf("/devices/" + device.id()), String.class);

        then(deleteAgainResponse.getStatusCode())
            .isEqualTo(NOT_FOUND);
    }

    private ReadDeviceResponse readDevice(String id) {
        return rest.getForObject(urlOf("/devices/" + id), ReadDeviceResponse.class);
    }

    private ReadCustomerResponse readCustomer(String id) {
        return rest.getForObject(urlOf("/customers/" + id), ReadCustomerResponse.class);
    }

    private UUID addSampleDevice() {
        final URI location = rest.postForLocation(urlOf("/devices"), device.toCreateRequest());
        return getIdFromLocation(location);
    }

    private void prepareSampleDevice() {
        final var id = addSampleDevice();
        device = device.withId(id.toString());
    }

    private SampleCustomer prepareSampleCustomer() {
        final URI location =
            rest.postForLocation(urlOf("/customers"), sampleCustomer.toCreateRequest());
        return sampleCustomer.withId(getIdFromLocation(location).toString());
    }

    private static UUID getIdFromLocation(URI location) {
        return UUID.fromString(substringAfterLast(location.getPath(), "/"));
    }
}
