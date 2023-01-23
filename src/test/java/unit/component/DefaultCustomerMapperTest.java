package unit.component;

import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullSource;

import io.jacopocav.customercare.component.DefaultCustomerMapper;
import io.jacopocav.customercare.dto.CustomerQueryResponse;
import io.jacopocav.customercare.dto.CustomerUpdateRequest;
import io.jacopocav.customercare.model.Customer;

class DefaultCustomerMapperTest {
    DefaultCustomerMapper underTest = new DefaultCustomerMapper();

    @Nested
    class IllegalArgumentsTest {

        @ParameterizedTest
        @NullSource
        void toDto_throws_givenIllegalArgument(Customer entity) {
            // when
            final var error = catchThrowable(() -> underTest.toDto(entity));

            // then
            then(error).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ArgumentsSource(ToEntity_IllegalArguments.class)
        void toEntity_throws_givenIllegalArguments(CustomerUpdateRequest request, Customer entity) {
            // when
            final var error = catchThrowable(() -> underTest.toEntity(request, entity));

            // then
            then(error).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class LegalArgumentsTest {

        @Test
        void toDto_returnsDtoEquivalentToGivenCustomer() {
            // given
            final var customerId = UUID.randomUUID();
            final var entity = new Customer()
                .setId(customerId)
                .setFirstName("John")
                .setLastName("Doe")
                .setFiscalCode("XXX")
                .setAddress("Country Road 66");

            final var expected = new CustomerQueryResponse(
                customerId.toString(),
                "John",
                "Doe",
                "XXX",
                "Country Road 66"
            );

            // when
            final CustomerQueryResponse actual = underTest.toDto(entity);

            // then
            then(actual).isEqualTo(expected);
        }

        @Test
        void toEntity_copiesFieldsOfGivenDtoIntoGivenEntity() {
            // given
            final var entity = new Customer()
                .setFirstName("John")
                .setLastName("Doe")
                .setFiscalCode("XXX")
                .setAddress("Old Address Road 36");

            final var newAddress = "New Address Avenue 42";
            final var dto = new CustomerUpdateRequest(newAddress);

            final var expected = new Customer()
                .setFirstName("John")
                .setLastName("Doe")
                .setFiscalCode("XXX")
                .setAddress(newAddress);

            // when
            underTest.toEntity(dto, entity);

            // then
            then(entity)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        }
    }

    static class ToEntity_IllegalArguments implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                arguments(null, null),
                arguments(null, new Customer()),
                arguments(new CustomerUpdateRequest(""), null)
            );
        }
    }
}
