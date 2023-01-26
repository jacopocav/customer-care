package unit.component;

import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;

import java.util.List;
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

import io.jacopocav.customercare.component.DefaultCustomerMapper;
import io.jacopocav.customercare.component.DeviceMapper;
import io.jacopocav.customercare.dto.CreateCustomerRequest;
import io.jacopocav.customercare.dto.ReadCustomerResponse;
import io.jacopocav.customercare.dto.ReadDeviceResponse;
import io.jacopocav.customercare.dto.UpdateCustomerRequest;
import io.jacopocav.customercare.model.Customer;
import io.jacopocav.customercare.model.Device;

@ExtendWith(MockitoExtension.class)
class DefaultCustomerMapperTest {
    @Mock DeviceMapper deviceMapper;
    DefaultCustomerMapper underTest;

    @BeforeEach
    void setUp() {
        underTest = new DefaultCustomerMapper(deviceMapper);
    }

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
        void toEntity_throws_givenIllegalArguments(UpdateCustomerRequest request, Customer entity) {
            // when
            final var error = catchThrowable(() -> underTest.toEntity(request, entity));

            // then
            then(error).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullSource
        void toNewEntity_throws_givenIllegalArgument(CreateCustomerRequest request) {
            // when
            final var error = catchThrowable(() -> underTest.toNewEntity(request));

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

            final var device1 = new Device().setColor("red");
            final var expectedDevice1 =
                new ReadDeviceResponse("", "", "red", customerId.toString());

            final var device2 = new Device().setColor("blue");
            final var expectedDevice2 =
                new ReadDeviceResponse("", "", "blue", customerId.toString());

            final var entity = new Customer()
                .setId(customerId)
                .setFirstName("John")
                .setLastName("Doe")
                .setFiscalCode("XXX")
                .setAddress("Country Road 66")
                .setDevices(List.of(device1, device2));

            final var expected = new ReadCustomerResponse(
                customerId.toString(),
                "John",
                "Doe",
                "XXX",
                "Country Road 66",
                List.of(expectedDevice1, expectedDevice2)
            );

            given(deviceMapper.toDto(device1))
                .willReturn(expectedDevice1);
            given(deviceMapper.toDto(device2))
                .willReturn(expectedDevice2);

            // when
            final ReadCustomerResponse actual = underTest.toDto(entity);

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
            final var dto = new UpdateCustomerRequest(newAddress);

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

        @Test
        void toNewEntity_returnsNewCustomerWithValuesCopiedFromRequest() {
            // given
            final var dto = new CreateCustomerRequest(
                "John",
                "Doe",
                "XXX",
                "Something Something Boulevard 33");

            final var expected = new Customer()
                .setFirstName("John")
                .setLastName("Doe")
                .setFiscalCode("XXX")
                .setAddress("Something Something Boulevard 33");

            // when
            final Customer actual = underTest.toNewEntity(dto);

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
                arguments(null, new Customer()),
                arguments(new UpdateCustomerRequest(""), null)
            );
        }
    }
}
