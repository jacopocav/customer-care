package io.jacopocav.customercare.rest.handler;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackFrames;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.util.List;
import java.util.Set;

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import io.jacopocav.customercare.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(LOWEST_PRECEDENCE)
@RestControllerAdvice
public class InternalExceptionHandler {

    public static final String DEBUG_HEADER = "x-debug";

    @ExceptionHandler
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse<List<String>> handle(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);

        final var moreInfo = isDebugEnabled(request)
            ? List.of(getStackFrames(ex))
            : null;

        return new ErrorResponse<>(
            "Internal error",
            ex.getMessage(),
            moreInfo
        );
    }

    private static final Set<String> DEBUG_ENABLED_VALUES = Set.of("true", "1", "on");

    private static boolean isDebugEnabled(WebRequest request) {
        final var header = defaultString(request.getHeader(DEBUG_HEADER), "false");
        return DEBUG_ENABLED_VALUES.contains(header.toLowerCase());
    }
}
