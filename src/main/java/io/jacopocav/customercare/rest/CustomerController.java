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
import io.jacopocav.customercare.dto.ReadCustomerResponse;
import io.jacopocav.customercare.dto.UpdateCustomerRequest;
import io.jacopocav.customercare.service.CustomerCrudService;
import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping
    public ResponseEntity<Void> create(
        @RequestBody CreateCustomerRequest body,
        HttpServletRequest request
    ) {
        final var newId = crudService.create(body).toString();
        final var location = ServletUriComponentsBuilder.fromRequest(request)
            .path("/{id}")
            .buildAndExpand(newId)
            .toUri();

        return created(location).build();
    }

    @GetMapping(path = "/{id}", consumes = ALL_VALUE)
    public ReadCustomerResponse read(@PathVariable @UUID String id) {
        return crudService.read(id);
    }

    @PatchMapping("/{id}")
    public void update(@PathVariable @UUID String id, @RequestBody UpdateCustomerRequest body) {
        crudService.update(id, body);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @UUID String id) {
        crudService.delete(id);
    }
}
