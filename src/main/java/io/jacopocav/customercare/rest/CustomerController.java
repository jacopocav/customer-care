package io.jacopocav.customercare.rest;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;

import org.hibernate.validator.constraints.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.jacopocav.customercare.dto.CreateCustomerRequest;
import io.jacopocav.customercare.dto.ErrorResponse;
import io.jacopocav.customercare.dto.ReadCustomerResponse;
import io.jacopocav.customercare.dto.UpdateCustomerRequest;
import io.jacopocav.customercare.service.CustomerCrudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping(
    path = "/customers",
    consumes = APPLICATION_JSON_VALUE,
    produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerCrudService crudService;

    @Operation(
        summary = "Creates a new customer",
        responses = {
            @ApiResponse(responseCode = "201", description = "Customer created successfully",
                headers = @Header(name = "Location", description = "URI of the new customer")),
            @ApiResponse(responseCode = "400", description = "Bad request",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @PostMapping
    public ResponseEntity<Void> create(
        @RequestBody @Valid CreateCustomerRequest body,
        HttpServletRequest request
    ) {
        final var newId = crudService.create(body).toString();
        final var location = ServletUriComponentsBuilder.fromRequest(request)
            .path("/{id}")
            .buildAndExpand(newId)
            .toUri();

        return created(location).build();
    }

    @Operation(
        summary = "Retrieves a customer",
        responses = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad request",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        })
    @GetMapping(path = "/{id}", consumes = ALL_VALUE)
    public ReadCustomerResponse read(@PathVariable @UUID String id) {
        return crudService.read(id);
    }

    @Operation(
        summary = "Updates a customer",
        responses = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        })
    @PatchMapping("/{id}")
    public void update(
        @PathVariable @UUID String id,
        @RequestBody @Valid UpdateCustomerRequest body
    ) {
        crudService.update(id, body);
    }

    @Operation(
        summary = "Deletes a customer",
        responses = {
            @ApiResponse(responseCode = "200", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        })
    @DeleteMapping(value = "/{id}", consumes = ALL_VALUE)
    public void delete(@PathVariable @UUID String id) {
        crudService.delete(id);
    }
}
