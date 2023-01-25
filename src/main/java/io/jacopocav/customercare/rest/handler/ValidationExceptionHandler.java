package io.jacopocav.customercare.rest.handler;

import static jakarta.validation.ElementKind.BEAN;
import static jakarta.validation.ElementKind.CONSTRUCTOR;
import static jakarta.validation.ElementKind.METHOD;
import static java.lang.String.format;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.jacopocav.customercare.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path.Node;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(1)
@RestControllerAdvice
public class ValidationExceptionHandler {
    private static final Set<ElementKind> ROOT_KINDS = Set.of(BEAN, METHOD, CONSTRUCTOR);
    private static final ErrorResponse<Void> RESPONSE =
        new ErrorResponse<>("Validation failed", null, null);

    private static void log(Throwable ex) {
        log.debug("Validation failed", ex);
    }

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse<Map<String, String>> handle(ConstraintViolationException e) {
        log(e);

        final Map<String, String> errorMap = e.getConstraintViolations().stream()
            .collect(groupingBy(this::getPropertyPath,
                mapping(ConstraintViolation::getMessage, joining(", "))));

        return RESPONSE.withAdditionalInfo(errorMap);
    }

    private String getPropertyPath(ConstraintViolation<?> cv) {
        final var path = cv.getPropertyPath();
        return stream(path.spliterator(), false)
            .dropWhile(node -> ROOT_KINDS.contains(node.getKind()))
            .map(Node::getName)
            .collect(joining("."));
    }

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse<Map<String, String>> handle(
        MissingServletRequestParameterException ex
    ) {
        log(ex);

        final var moreInfo = Map.of(
            ex.getParameterName(),
            format("required request parameter of type %s is not present", ex.getParameterType())
        );

        return RESPONSE.withAdditionalInfo(moreInfo);
    }

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse<Void> handle(HttpMessageNotReadableException ex) {
        log.debug("Could not parse request body", ex);

        return RESPONSE.withDescription(ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse<Map<String, String>> handle(BindException ex) {
        log(ex);

        final String globalErrors = ex.getGlobalErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(joining(", "));

        final Map<String, String> fieldErrors = ex.getFieldErrors().stream()
            .filter(err -> err.getDefaultMessage() != null)
            .collect(groupingBy(FieldError::getField,
                mapping(DefaultMessageSourceResolvable::getDefaultMessage, joining(", "))));

        final Map<String, String> moreInfo;
        if (!globalErrors.isBlank()) {
            moreInfo = new HashMap<>();
            moreInfo.put("global-errors", globalErrors);
            moreInfo.putAll(fieldErrors);
        } else {
            moreInfo = fieldErrors;
        }

        return RESPONSE.withAdditionalInfo(moreInfo);
    }

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse<Map<String, Object>> handle(MethodArgumentTypeMismatchException ex) {
        final var moreInfo = new HashMap<String, Object>();
        moreInfo.put("parameter", ex.getName());
        moreInfo.put("value", ex.getValue());
        moreInfo.put("requiredType",
            requireNonNullElse(ex.getRequiredType(), Object.class).getSimpleName());

        return RESPONSE
            .withDescription("Parameter cannot be parsed to the correct type")
            .withAdditionalInfo(moreInfo);
    }
}
