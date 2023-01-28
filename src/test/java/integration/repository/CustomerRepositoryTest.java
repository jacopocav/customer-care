package integration.repository;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import io.jacopocav.customercare.CustomerCareApplication;
import io.jacopocav.customercare.model.Customer;
import io.jacopocav.customercare.model.Device;
import io.jacopocav.customercare.model.Device.Status;
import io.jacopocav.customercare.repository.CustomerRepository;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@ContextConfiguration(classes = CustomerCareApplication.class)
class CustomerRepositoryTest {
    @Autowired CustomerRepository underTest;
    @Autowired TestEntityManager entityManager;

    @Test
    void findFetchingDevicesById_givenNoDeviceIsAssociatedToCustomer() {
        // given
        final var customer = new Customer()
            .setFirstName("Mary")
            .setLastName("Brown")
            .setFiscalCode("some fiscal code")
            .setAddress("Random Road 101");

        final var id = entityManager.persistAndGetId(customer, UUID.class);
        final var expected = new Customer()
            .setId(id)
            .setFirstName("Mary")
            .setLastName("Brown")
            .setFiscalCode("some fiscal code")
            .setAddress("Random Road 101");

        entityManager.flush();

        // when
        final var actual = underTest.findFetchingDevicesById(id);

        // then
        then(actual)
            .get()
            .usingRecursiveComparison()
            .ignoringFields("createdAt", "lastModifiedAt")
            .isEqualTo(expected);
    }

    @Test
    void findFetchingDevicesById_givenSomeDeviceIsAssociatedToCustomer() {
        // given

        final var customer = new Customer()
            .setFirstName("Mary")
            .setLastName("Brown")
            .setFiscalCode("some fiscal code")
            .setAddress("Random Road 101");

        final var device = new Device()
            .setColor("red")
            .setStatus(Status.ACTIVE)
            .setCustomer(customer);

        final var customerId = entityManager.persistAndGetId(customer, UUID.class);
        final var expected = new Customer()
            .setId(customerId)
            .setFirstName("Mary")
            .setLastName("Brown")
            .setFiscalCode("some fiscal code")
            .setAddress("Random Road 101")
            .setDevices(List.of(device));

        entityManager.persistAndFlush(device);
        entityManager.clear();

        // when
        final var actual = underTest.findFetchingDevicesById(customerId);

        // then
        then(actual)
            .isPresent();

        final var actualCustomer = actual.get();

        then(actualCustomer)
            .usingRecursiveComparison()
            .ignoringFields("devices", "createdAt", "lastModifiedAt")
            .isEqualTo(expected);

        then(actualCustomer.getDevices())
            .hasSize(1);

        final var actualDevice = actualCustomer.getDevices().get(0);

        then(actualDevice)
            .usingRecursiveComparison()
            .ignoringFields("customer", "createdAt", "lastModifiedAt")
            .isEqualTo(device);

        then(actualCustomer)
            .isSameAs(actualDevice.getCustomer());
    }
}
