package io.jacopocav.customercare.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(NON_ABSENT)
public record ErrorResponse<T>(
    String summary,
    String description,
    T additionalInfo
) {

    public ErrorResponse<T> withDescription(String details) {
        return new ErrorResponse<>(summary, details, additionalInfo);
    }

    public <S> ErrorResponse<S> withAdditionalInfo(S metadata) {
        return new ErrorResponse<>(summary, description, metadata);
    }
}
