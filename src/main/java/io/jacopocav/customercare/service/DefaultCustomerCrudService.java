package io.jacopocav.customercare.service;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.jacopocav.customercare.component.CustomerMapper;
import io.jacopocav.customercare.dto.CreateCustomerRequest;
import io.jacopocav.customercare.dto.ReadCustomerResponse;
import io.jacopocav.customercare.dto.UpdateCustomerRequest;
import io.jacopocav.customercare.error.CustomerNotFoundException;
import io.jacopocav.customercare.model.Customer;
import io.jacopocav.customercare.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DefaultCustomerCrudService implements CustomerCrudService {
    private final CustomerMapper mapper;
    private final CustomerRepository repository;

    @Override
    public UUID create(CreateCustomerRequest request) {
        Assert.notNull(request, "request is null");

        final Customer customer = mapper.toNewEntity(request);
        return repository.save(customer).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public ReadCustomerResponse read(String id) {
        requireNotBlank(id, "id");

        final var uuid = UUID.fromString(id);
        final var customer = repository.findFetchingDevicesById(uuid)
            .orElseThrow(() -> new CustomerNotFoundException(uuid));

        return mapper.toDto(customer);
    }

    @Override
    public void update(String id, UpdateCustomerRequest request) {
        requireNotBlank(id, "id");
        Assert.notNull(request, "request is null");

        final var customer = findCustomer(id);
        mapper.toEntity(request, customer);
    }

    @Override
    public void delete(String id) {
        requireNotBlank(id, "id");

        final var customer = findCustomer(id);
        repository.delete(customer);
    }

    private Customer findCustomer(String id) {
        final var uuid = UUID.fromString(id);
        return repository.findById(uuid)
            .orElseThrow(() -> new CustomerNotFoundException(uuid));
    }

    private static void requireNotBlank(String value, String name) {
        Assert.isTrue(isNotBlank(value), name + " is blank or null");
    }
}
