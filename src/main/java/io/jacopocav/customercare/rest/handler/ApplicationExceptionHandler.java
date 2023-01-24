package io.jacopocav.customercare.rest.handler;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jacopocav.customercare.dto.ErrorResponse;
import io.jacopocav.customercare.error.CustomerNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApplicationExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(NOT_FOUND)
    public ErrorResponse<Void> handle(CustomerNotFoundException ex) {
        log.debug("Customer not found", ex);

        return new ErrorResponse<>("Customer not found",
            "Could not find customer with id " + ex.getIdentifier(),
            null);
    }
}
