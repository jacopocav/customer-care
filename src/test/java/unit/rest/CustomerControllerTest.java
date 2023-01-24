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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockServletContext;

import io.jacopocav.customercare.dto.CustomerCreationRequest;
import io.jacopocav.customercare.dto.CustomerQueryResponse;
import io.jacopocav.customercare.dto.CustomerUpdateRequest;
import io.jacopocav.customercare.rest.CustomerController;
import io.jacopocav.customercare.service.CustomerCrudService;

@SuppressWarnings("AccessStaticViaInstance")
@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {
    @Mock CustomerCrudService crudService;
    CustomerController underTest;

    @BeforeEach
    void setUp() {
        underTest = new CustomerController(crudService);
    }

    @Test
    void create() {
        // given
        final var body = new CustomerCreationRequest("John", "Doe", "ABCX", "Some Road 101");
        final var servletRequest = post(URI.create("https://www.some-base-url.com:1234/customers"))
            .buildRequest(new MockServletContext());
        final var newId = UUID.randomUUID();

        final var expectedLocation = "https://www.some-base-url.com:1234/customers/" + newId;
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
            new CustomerQueryResponse(id, "Mary", "Doe", "4321X", "Any Road 66");

        given(crudService.read(id))
            .willReturn(expected);

        // when
        final CustomerQueryResponse actual = underTest.read(id);

        // then
        and.then(actual).isEqualTo(expected);
    }

    @Test
    void update() {
        // given
        final var id = "12345";
        final var request =
            new CustomerUpdateRequest("New Road 72");

        // when
        underTest.update(id, request);

        // then
        then(crudService).should().update(id, request);
    }
}
