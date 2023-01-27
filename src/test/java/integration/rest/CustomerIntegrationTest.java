package integration.rest;

import static integration.rest.SampleCustomer.sample;
import static org.apache.commons.lang3.StringUtils.stripStart;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.net.URI;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import io.jacopocav.customercare.CustomerCareApplication;
import io.jacopocav.customercare.dto.ReadCustomerResponse;
import io.jacopocav.customercare.dto.UpdateCustomerRequest;

@ActiveProfiles("test")
@SpringBootTest(classes = CustomerCareApplication.class, webEnvironment = RANDOM_PORT)
class CustomerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired TestRestTemplate rest;

    @Test
    void create() {
        // create customer
        final ResponseEntity<String> createResponse = createSampleCustomer();

        then(createResponse.getStatusCode())
            .isEqualTo(CREATED);
        then(createResponse.getHeaders().getLocation())
            .isNotNull();

        // read created customer
        final var location = createResponse.getHeaders().getLocation();
        final var id = UUID.fromString(substringAfterLast(location.toString(), "/"));

        final var readResult = readCustomer(id).getBody();

        then(readResult)
            .isEqualTo(sample.withId(id.toString()).toReadResponse());
    }

    @Test
    void read() {
        // create customer
        final UUID id = prepareSampleCustomer();

        // read non-existing customer
        final var notFoundResponse = readCustomer(UUID.randomUUID());

        then(notFoundResponse.getStatusCode())
            .isEqualTo(NOT_FOUND);

        // read existing customer
        final ResponseEntity<ReadCustomerResponse> readResponse = readCustomer(id);

        then(readResponse.getStatusCode())
            .isEqualTo(OK);
        then(readResponse.getBody())
            .isEqualTo(sample.withId(id.toString()).toReadResponse());
    }

    @Test
    void update() {
        // create customer
        final var oldAddress = "Old Road 6";
        final var newAddress = "New Road 4";
        final UUID id = prepareSampleCustomer(oldAddress);
        final var expected = sample
            .withId(id.toString())
            .withAddress(newAddress)
            .toReadResponse();

        // update customer
        final ResponseEntity<String> updateResult = updateCustomer(id, newAddress);

        then(updateResult.getStatusCode())
            .isEqualTo(OK);

        // read updated customer
        final ReadCustomerResponse readResult = readCustomer(id).getBody();

        then(readResult)
            .isEqualTo(expected);
    }

    @Test
    void delete() {
        // create customer
        final UUID id = prepareSampleCustomer();

        // delete customer
        final ResponseEntity<String> deleteResult1 = deleteCustomer(id);

        then(deleteResult1.getStatusCode())
            .isEqualTo(OK);

        // delete customer again
        final ResponseEntity<String> deleteResult2 = deleteCustomer(id);

        then(deleteResult2.getStatusCode())
            .isEqualTo(NOT_FOUND);

        // read deleted customer
        final var readResult = readCustomer(id);

        then(readResult.getStatusCode())
            .isEqualTo(NOT_FOUND);
    }

    private UUID prepareSampleCustomer() {
        return prepareSampleCustomer(sample.address());
    }

    private UUID prepareSampleCustomer(String address) {
        final URI location = createSampleCustomer(address).getHeaders().getLocation();
        then(location).isNotNull();
        return UUID.fromString(substringAfterLast(location.toString(), "/"));
    }

    private ResponseEntity<String> deleteCustomer(UUID id) {
        return rest.exchange(urlOf("/customers/" + id), DELETE, null, String.class);
    }

    private ResponseEntity<String> updateCustomer(UUID id, String newAddress) {
        final var updateRequest = new UpdateCustomerRequest(newAddress);
        return rest.exchange(urlOf("/customers/" + id), PATCH,
            new HttpEntity<>(updateRequest), String.class);
    }

    private ResponseEntity<ReadCustomerResponse> readCustomer(UUID id) {
        return rest.getForEntity(urlOf("/customers/" + id), ReadCustomerResponse.class);
    }

    private ResponseEntity<String> createSampleCustomer() {
        return createSampleCustomer(sample.address());
    }

    private ResponseEntity<String> createSampleCustomer(String address) {
        final var customer = sample.withAddress(address).toCreateRequest();
        return rest.postForEntity(urlOf("/customers"), customer, String.class);
    }

    private URI urlOf(String resource) {
        final var string = "http://localhost:%d/%s".formatted(port, stripStart(resource, "/"));
        return URI.create(string);
    }
}
