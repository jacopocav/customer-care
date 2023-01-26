package io.jacopocav.customercare.rest.handler;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jacopocav.customercare.dto.ErrorResponse;
import io.jacopocav.customercare.error.CustomerNotFoundException;
import io.jacopocav.customercare.error.DeviceLimitReachedException;
import io.jacopocav.customercare.error.DeviceNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(0)
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

    @ExceptionHandler
    @ResponseStatus(NOT_FOUND)
    public ErrorResponse<Void> handle(DeviceNotFoundException ex) {
        log.debug("Device not found", ex);

        return new ErrorResponse<>("Device not found",
            "Could not find device with id " + ex.getIdentifier(),
            null);
    }

    @ExceptionHandler
    @ResponseStatus(NOT_FOUND)
    public ErrorResponse<Void> handle(DeviceLimitReachedException ex) {
        log.debug("Device limit reached", ex);

        return new ErrorResponse<>("Device limit reached",
            ex.getMessage(),
            null);
    }
}
