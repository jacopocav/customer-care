package unit.rest;

import static org.assertj.core.api.BDDAssertions.and;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.net.URI;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockServletContext;

import io.jacopocav.customercare.dto.CreateDeviceRequest;
import io.jacopocav.customercare.dto.ReadDeviceResponse;
import io.jacopocav.customercare.dto.UpdateDeviceRequest;
import io.jacopocav.customercare.rest.DeviceController;
import io.jacopocav.customercare.service.DeviceCrudService;

@SuppressWarnings("AccessStaticViaInstance")
@ExtendWith(MockitoExtension.class)
class DeviceControllerTest {
    @Mock DeviceCrudService crudService;
    DeviceController underTest;

    @BeforeEach
    void setUp() {
        underTest = new DeviceController(crudService);
    }

    @Test
    void create() {
        // given
        final var body = new CreateDeviceRequest("customerId", "ACTIVE", "#aabbcc");
        final var servletRequest = post(URI.create("https://www.some-base-url.com:1234/devices"))
            .buildRequest(new MockServletContext());
        final var newId = UUID.randomUUID();

        final var expectedLocation = "https://www.some-base-url.com:1234/devices/" + newId;
        final var expectedHeaders = new HttpHeaders();
        expectedHeaders.add("Location", expectedLocation);

        given(crudService.create(body))
            .willReturn(newId);

        // when
        final ResponseEntity<Void> actual = underTest.create(body, servletRequest);

        // then
        and.then(actual.getStatusCode()).isEqualTo(CREATED);
        and.then(actual.getBody()).isNull();
        and.then(actual.getHeaders()).isEqualTo(expectedHeaders);
    }

    @Test
    void read() {
        // given
        final var id = "12345";
        final var expected =
            new ReadDeviceResponse(id, "INACTIVE", "#ffffff", "customerId");

        given(crudService.read(id))
            .willReturn(expected);

        // when
        final ReadDeviceResponse actual = underTest.read(id);

        // then
        and.then(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "true,OK",
        "false,NOT_FOUND"
    })
    void exists(boolean exists, HttpStatus expected) {
        // given
        final var id = "12345";

        given(crudService.exists(id))
            .willReturn(exists);

        // when
        final ResponseEntity<Void> actual = underTest.exists(id);

        // then
        and.then(actual.getStatusCode()).isEqualTo(expected);
    }

    @Test
    void update() {
        // given
        final var id = "12345";
        final var request =
            new UpdateDeviceRequest("LOST", "11aa22");

        // when
        underTest.update(id, request);

        // then
        then(crudService).should().update(id, request);
    }

    @Test
    void delete() {
        // given
        final var id = "12345";

        // when
        underTest.delete(id);

        // then
        then(crudService).should().delete(id);
    }
}
