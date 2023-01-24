package io.jacopocav.customercare.service;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.jacopocav.customercare.component.CustomerMapper;
import io.jacopocav.customercare.dto.CustomerCreationRequest;
import io.jacopocav.customercare.dto.CustomerQueryResponse;
import io.jacopocav.customercare.dto.CustomerUpdateRequest;
import io.jacopocav.customercare.error.ResourceNotFoundException;
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
    public UUID create(CustomerCreationRequest request) {
        Assert.notNull(request, "request is null");

        final Customer customer = mapper.toNewEntity(request);
        return repository.save(customer).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerQueryResponse read(String id) {
        Assert.isTrue(isNotBlank(id), "id is blank or null");

        final var customer = findCustomer(id);
        return mapper.toDto(customer);
    }

    @Override
    public void update(String id, CustomerUpdateRequest request) {
        Assert.isTrue(isNotBlank(id), "id is blank or null");
        Assert.notNull(request, "request is null");

        final var customer = findCustomer(id);
        mapper.toEntity(request, customer);
    }

    private Customer findCustomer(String id) {
        final var uuid = UUID.fromString(id);
        return repository.findById(uuid)
            .orElseThrow(() -> new ResourceNotFoundException(uuid));
    }
}
