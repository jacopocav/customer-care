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
class AuditTest {
    @Autowired TestEntityManager entityManager;

    @Test
    void auditColumnsAreAutomaticallyPopulated() {
        // given
        final var entity = new Customer()
            .setFirstName("John")
            .setLastName("Doe")
            .setAddress("Roadhouse 204")
            .setFiscalCode("123");

        // when
        entityManager.persistAndFlush(entity);
        final var lastModifiedBefore = entity.getLastModifiedAt();

        entity.setAddress("New Road 123");

        entityManager.persistAndFlush(entity);
        final var lastModifiedAfter = entity.getLastModifiedAt();

        // then
        then(entity.getCreatedAt())
            .isNotNull();
        then(lastModifiedBefore)
            .isNotNull();
        then(lastModifiedAfter)
            .isNotNull()
            .isAfter(lastModifiedBefore);
    }
}
