package integration.rest.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import integration.rest.handler.ValidationExceptionHandlerTest.Config.TestController;
import io.jacopocav.customercare.dto.ErrorResponse;
import io.jacopocav.customercare.error.CustomerNotFoundException;
import io.jacopocav.customercare.error.DeviceLimitReachedException;
import io.jacopocav.customercare.error.DeviceNotFoundException;
import io.jacopocav.customercare.rest.handler.ApplicationExceptionHandler;
import lombok.RequiredArgsConstructor;

@ActiveProfiles("test")
@WebMvcTest(TestController.class)
class ApplicationExceptionHandlerTest {

    @Autowired ObjectMapper mapper;
    @Autowired MockMvc mockMvc;

    @Test
    void handle_customerNotFoundException() throws Exception {
        // given
        final var id = UUID.randomUUID();
        final var expected = new ErrorResponse<>(
            "Customer not found",
            "Could not find customer with id " + id,
            null
        );

        // when
        final var result = mockMvc.perform(get("/test/customer")
            .queryParam("id", id.toString()));

        // then
        result
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(mapper.writeValueAsString(expected)));
    }

    @Test
    void handle_deviceNotFoundException() throws Exception {
        // given
        final var id = UUID.randomUUID();
        final var expected = new ErrorResponse<>(
            "Device not found",
            "Could not find device with id " + id,
            null
        );

        // when
        final var result = mockMvc.perform(get("/test/device")
            .queryParam("id", id.toString()));

        // then
        result
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(mapper.writeValueAsString(expected)));
    }

    @Test
    void handle_deviceLimitReachedException() throws Exception {
        // given
        final var customerId = UUID.randomUUID();
        final var limit = 42;
        final var expected = new ErrorResponse<>(
            "Device limit reached",
            "customer %s already has the maximum allowed number of devices (%d)"
                .formatted(customerId, limit),
            null
        );

        // when
        final var result = mockMvc.perform(get("/test/device/limit")
            .queryParam("id", customerId.toString())
            .queryParam("limit", Integer.toString(limit)));

        // then
        result
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(mapper.writeValueAsString(expected)));
    }

    @Configuration
    static class Config {
        @Bean
        ApplicationExceptionHandler underTest() {
            return new ApplicationExceptionHandler();
        }

        @Validated
        @RestController
        @RequiredArgsConstructor
        @SuppressWarnings("unused")
        @RequestMapping(path = "/test", produces = APPLICATION_JSON_VALUE)
        static class TestController {
            @GetMapping("/customer")
            String customerNotFound(@RequestParam String id) {
                throw new CustomerNotFoundException(UUID.fromString(id));
            }

            @GetMapping("/device")
            String deviceNotFound(@RequestParam String id) {
                throw new DeviceNotFoundException(UUID.fromString(id));
            }

            @GetMapping("/device/limit")
            String deviceLimitReached(@RequestParam String id, @RequestParam int limit) {
                throw new DeviceLimitReachedException(limit, UUID.fromString(id));
            }
        }
    }
}
