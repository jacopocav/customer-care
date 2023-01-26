package integration.model;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import io.jacopocav.customercare.CustomerCareApplication;
import io.jacopocav.customercare.model.Customer;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@ContextConfiguration(classes = CustomerCareApplication.class)
class CommonModelAuditTest {
    @Autowired TestEntityManager entityManager;

    @Test
    void versionColumnIsIncrementedAtEveryFlushedChange() {
        // given
        final var entity = aCustomer();

        // when
        entityManager.persistAndFlush(entity);
        final var versionBefore = entity.getVersion();

        entity.setAddress("New Road 123");

        entityManager.persistAndFlush(entity);
        final var versionAfter = entity.getVersion();

        // then
        then(versionAfter)
            .isEqualTo(versionBefore + 1);
    }

    @Test
    void createdAtColumnIsPopulatedOnce() {
        // given
        final var entity = aCustomer();

        // when
        entityManager.persistAndFlush(entity);
        final var createdAtBefore = entity.getCreatedAt();

        entity.setAddress("New Road 123");

        entityManager.persistAndFlush(entity);
        final var createdAtAfter = entity.getCreatedAt();

        // then
        then(createdAtAfter)
            .isNotNull()
            .isEqualTo(createdAtBefore);
    }

    @Test
    void lastModifiedAtIsAutomaticallyPopulatedAtEveryFlushedChange() {
        // given
        final var entity = aCustomer();

        // when
        entityManager.persistAndFlush(entity);
        final var lastModifiedAtBefore = entity.getLastModifiedAt();

        entity.setAddress("New Road 123");

        entityManager.persistAndFlush(entity);
        final var lastModifiedAtAfter = entity.getLastModifiedAt();

        // then
        then(lastModifiedAtBefore)
            .isNotNull();
        then(lastModifiedAtAfter)
            .isNotNull()
            .isAfter(lastModifiedAtBefore);
    }

    private static Customer aCustomer() {
        return new Customer()
            .setFirstName("John")
            .setLastName("Doe")
            .setAddress("Roadhouse 204")
            .setFiscalCode("123");
    }
}
