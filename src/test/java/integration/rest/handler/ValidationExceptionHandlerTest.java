package integration.rest.handler;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import integration.rest.handler.ValidationExceptionHandlerTest.Config.TestController;
import io.jacopocav.customercare.dto.ErrorResponse;
import io.jacopocav.customercare.rest.handler.ValidationExceptionHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@ActiveProfiles("test")
@WebMvcTest(TestController.class)
class ValidationExceptionHandlerTest {

    @Autowired ObjectMapper mapper;
    @Autowired MockMvc mockMvc;

    @Test
    void handle_constraintValidationException() throws Exception {
        // given
        final var expected = new ErrorResponse<>(
            "Validation failed",
            null,
            Map.of(
                "param", "must not be blank",
                "anotherParam", "must be greater than 0"
            )
        );

        // when
        final var result = mockMvc.perform(get("/test/validation")
            .queryParam("param", "")
            .queryParam("anotherParam", "-1"));

        // then
        result
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(mapper.writeValueAsString(expected)));
    }

    @Test
    void handle_missingServletRequestParameterException() throws Exception {
        // given
        final var expected = new ErrorResponse<>(
            "Validation failed",
            null,
            Map.of("param", "required request parameter of type String is not present")
        );

        // when
        final var result = mockMvc.perform(get("/test/validation")
            .queryParam("anotherParam", "1"));

        // then
        result
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(mapper.writeValueAsString(expected)));
    }

    @Test
    void handle_bindException() throws Exception {
        // given
        final var body = new Body(null);

        final var expected = new ErrorResponse<>(
            "Validation failed",
            null, Map.of(
            "field", "must not be blank"
        ));

        // when
        final var result = mockMvc.perform(post("/test/validation")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)));

        // then
        result
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(mapper.writeValueAsString(expected)));
    }

    @Test
    void handle_httpMessageNotReadableException() throws Exception {
        // given
        final var invalidBody = "{\"field\": 42";
        final var expected = new ErrorResponse<>("Validation failed", null, null);

        // when
        final var result = mockMvc.perform(post("/test/validation")
            .contentType(APPLICATION_JSON)
            .content(invalidBody));

        // then
        result
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(APPLICATION_JSON));

        final var responseString = result.andReturn().getResponse().getContentAsString();
        final var response = mapper.readValue(responseString, ErrorResponse.class);

        then(response)
            .usingRecursiveComparison()
            .ignoringFields("description")
            .isEqualTo(expected);

        then(response.description())
            .containsIgnoringCase("json parse error");
    }

    @Test
    void handle_methodArgumentTypeMismatchException() throws Exception {
        // given
        final var expected = new ErrorResponse<>(
            "Validation failed",
            "Parameter cannot be parsed to the correct type",
            Map.of(
                "parameter", "anotherParam",
                "value", "not-a-number",
                "requiredType", "int"
            ));

        // when
        final var result = mockMvc.perform(get("/test/validation")
            .queryParam("param", "value")
            .queryParam("anotherParam", "not-a-number"));

        // then
        result
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(mapper.writeValueAsString(expected)));
    }

    @Configuration
    static class Config {
        @Bean
        ValidationExceptionHandler underTest() {
            return new ValidationExceptionHandler();
        }

        @Validated
        @RestController
        @RequiredArgsConstructor
        @SuppressWarnings("unused")
        @RequestMapping(path = "/test", produces = APPLICATION_JSON_VALUE)
        static class TestController {
            @GetMapping("/validation")
            int validation(
                @RequestParam @NotBlank String param,
                @RequestParam @Positive int anotherParam
            ) {
                return 42;
            }

            @PostMapping(path = "/validation", consumes = APPLICATION_JSON_VALUE)
            long bodyValidation(@RequestBody @Valid Body body) {
                return 42;
            }
        }
    }

    record Body(@NotBlank String field) {}
}
