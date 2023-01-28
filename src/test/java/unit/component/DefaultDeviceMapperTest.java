package unit.component;

import static io.jacopocav.customercare.model.Device.Status.ACTIVE;
import static io.jacopocav.customercare.model.Device.Status.INACTIVE;
import static io.jacopocav.customercare.model.Device.Status.LOST;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.jacopocav.customercare.component.DefaultDeviceMapper;
import io.jacopocav.customercare.dto.CreateDeviceRequest;
import io.jacopocav.customercare.dto.ReadDeviceResponse;
import io.jacopocav.customercare.dto.UpdateDeviceRequest;
import io.jacopocav.customercare.error.CustomerNotFoundException;
import io.jacopocav.customercare.error.ResourceNotFoundException;
import io.jacopocav.customercare.model.Customer;
import io.jacopocav.customercare.model.Device;
import io.jacopocav.customercare.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class DefaultDeviceMapperTest {
    @Mock CustomerRepository customerRepository;
    DefaultDeviceMapper underTest;

    @BeforeEach
    void setUp() {
        underTest = new DefaultDeviceMapper(customerRepository);
    }

    @Nested
    class IllegalArgumentsTest {

        @ParameterizedTest
        @NullSource
        void toDto_throws_givenIllegalArgument(Device entity) {
            // when
            final var error = catchThrowable(() -> underTest.toDto(entity));

            // then
            then(error).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ArgumentsSource(ToEntity_IllegalArguments.class)
        void toEntity_throws_givenIllegalArguments(UpdateDeviceRequest request, Device entity) {
            // when
            final var error = catchThrowable(() -> underTest.toEntity(request, entity));

            // then
            then(error).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullSource
        void toNewEntity_throws_givenIllegalArgument(CreateDeviceRequest request) {
            // when
            final var error = catchThrowable(() -> underTest.toNewEntity(request));

            // then
            then(error).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class LegalArgumentsTest {

        @Test
        void toDto_returnsDtoEquivalentToGivenDevice() {
            // given
            final var deviceId = UUID.randomUUID();
            final var customerId = UUID.randomUUID();
            final var customer = new Customer()
                .setId(customerId);
            final var entity = new Device()
                .setId(deviceId)
                .setCustomer(customer)
                .setStatus(ACTIVE)
                .setColor("aabbcc");

            final var expected = new ReadDeviceResponse(
                deviceId.toString(),
                "ACTIVE",
                "#aabbcc",
                customerId.toString()
            );

            // when
            final ReadDeviceResponse actual = underTest.toDto(entity);

            // then
            then(actual).isEqualTo(expected);
        }

        @Test
        void toEntity_copiesFieldsOfGivenDtoIntoGivenEntity() {
            // given
            final var entity = new Device()
                .setStatus(INACTIVE)
                .setColor("abcdef");

            final var dto = new UpdateDeviceRequest(
                "lost",
                "#012345"
            );

            final var expected = new Device()
                .setStatus(LOST)
                .setColor("012345");

            // when
            underTest.toEntity(dto, entity);

            // then
            then(entity)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        }

        @Test
        void toNewEntity_throws_givenCustomerNotFound() {
            // given
            final var customerId = UUID.randomUUID();

            final var dto = new CreateDeviceRequest(
                customerId.toString(),
                "active",
                "aBcDeF"
            );

            given(customerRepository.findById(customerId))
                .willReturn(Optional.empty());

            // when
            final var error = catchThrowable(() -> underTest.toNewEntity(dto));

            // then
            then(error)
                .asInstanceOf(type(CustomerNotFoundException.class))
                .extracting(ResourceNotFoundException::getIdentifier)
                .isEqualTo(customerId);
        }

        @Test
        void toNewEntity_returnsNewDeviceWithValuesCopiedFromRequest() {
            // given
            final var customerId = UUID.randomUUID();
            final var customer = new Customer().setId(customerId);

            final var dto = new CreateDeviceRequest(
                customerId.toString(),
                "active",
                "aBcDeF"
            );

            final var expected = new Device()
                .setCustomer(customer)
                .setStatus(ACTIVE)
                .setColor("abcdef");

            given(customerRepository.findById(customerId))
                .willReturn(Optional.of(customer));

            // when
            final Device actual = underTest.toNewEntity(dto);

            // then
            then(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        }
    }

    static class ToEntity_IllegalArguments implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                arguments(null, null),
                arguments(null, new Device()),
                arguments(new UpdateDeviceRequest("active", "ffffff"), null)
            );
        }
    }
}
