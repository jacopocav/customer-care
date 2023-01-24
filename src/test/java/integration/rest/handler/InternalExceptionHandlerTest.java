package integration.rest.handler;

import static io.jacopocav.customercare.rest.handler.InternalExceptionHandler.DEBUG_HEADER;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.iterable;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import integration.rest.handler.ValidationExceptionHandlerTest.Config.TestController;
import io.jacopocav.customercare.dto.ErrorResponse;
import io.jacopocav.customercare.rest.handler.InternalExceptionHandler;
import lombok.RequiredArgsConstructor;

@ActiveProfiles("test")
@WebMvcTest(TestController.class)
class InternalExceptionHandlerTest {

    @Autowired ObjectMapper mapper;
    @Autowired MockMvc mockMvc;

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"off", "false", "0"})
    void handle_uncaughtException_withoutStackTrace(String headerValue) throws Exception {
        // given
        final var expected = new ErrorResponse<>("Internal error", "test message", null);
        final var headers = new HttpHeaders();
        if (headerValue != null) {
            headers.add(DEBUG_HEADER, headerValue);
        }
        // when
        final var result = mockMvc.perform(get("/test/uncaught").headers(headers));

        // then
        result
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(mapper.writeValueAsString(expected)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "True", "1", "on", "ON"})
    void handle_uncaughtException_withStackTrace(String headerValue) throws Exception {
        // given
        final var expected = new ErrorResponse<>("Internal error", "test message", null);

        // when
        final var result = mockMvc.perform(get("/test/uncaught")
            .header(DEBUG_HEADER, headerValue));

        // then
        result
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(APPLICATION_JSON));

        final var responseString = result.andReturn().getResponse().getContentAsString();
        final var actual = mapper.readValue(responseString, ErrorResponse.class);

        then(actual)
            .usingRecursiveComparison()
            .ignoringFields("additionalInfo")
            .isEqualTo(expected);

        then(actual.additionalInfo())
            .asInstanceOf(iterable(String.class))
            .isNotEmpty();
    }

    @Configuration
    static class Config {
        @Bean
        InternalExceptionHandler underTest() {
            return new InternalExceptionHandler();
        }

        @Validated
        @RestController
        @RequiredArgsConstructor
        @SuppressWarnings("unused")
        @RequestMapping(path = "/test", produces = APPLICATION_JSON_VALUE)
        static class TestController {
            @GetMapping("/uncaught")
            String uncaught() {
                throw new IllegalStateException("test message");
            }
        }
    }
}
