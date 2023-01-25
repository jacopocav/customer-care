package unit.service;

import static org.assertj.core.api.BDDAssertions.and;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.thenNoException;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.jacopocav.customercare.component.DeviceMapper;
import io.jacopocav.customercare.dto.CreateDeviceRequest;
import io.jacopocav.customercare.dto.ReadDeviceResponse;
import io.jacopocav.customercare.dto.UpdateDeviceRequest;
import io.jacopocav.customercare.error.DeviceLimitReachedException;
import io.jacopocav.customercare.error.DeviceNotFoundException;
import io.jacopocav.customercare.model.Device;
import io.jacopocav.customercare.repository.DeviceRepository;
import io.jacopocav.customercare.service.DefaultDeviceCrudService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("AccessStaticViaInstance")
class DefaultDeviceCrudServiceTest {
    static final int DEVICE_LIMIT = 42;
    @Mock DeviceMapper mapper;
    @Mock DeviceRepository repository;

    DefaultDeviceCrudService underTest;

    @BeforeEach
    void setUp() {
        underTest = new DefaultDeviceCrudService(mapper, repository, DEVICE_LIMIT);
    }

    @Nested
    @SuppressWarnings("DataFlowIssue")
    class IllegalArgumentsTest {
        @Test
        void create_throws_givenNullRequest() {
            // when
            final var error = catchThrowable(() -> underTest.create(null));

            // then
            and.then(error).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ArgumentsSource(IllegalIds.class)
        void read_throws_givenIllegalId(String illegalId) {
            // when
            final var error = catchThrowable(() -> underTest.read(illegalId));

            // then
            and.then(error).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ArgumentsSource(Update_IllegalArguments.class)
        void update_throws_givenIllegalArguments(String id, UpdateDeviceRequest request) {
            // when
            final var error = catchThrowable(() -> underTest.update(id, request));

            // then
            and.then(error).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ArgumentsSource(IllegalIds.class)
        void delete_throws_givenIllegalArguments(String id) {
            // when
            final var error = catchThrowable(() -> underTest.delete(id));

            // then
            and.then(error).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class LegalArgumentsTest {
        @Test
        void create_throws_givenLimitReachedForGivenCustomer() {
            // given
            final var customerId = UUID.randomUUID();
            final var request = new CreateDeviceRequest(customerId.toString(), "", "");

            given(repository.countByCustomerId(customerId))
                .willReturn(DEVICE_LIMIT);

            // when
            final var error = catchThrowable(() -> underTest.create(request));

            // then
            then(mapper).shouldHaveNoInteractions();
            then(repository).should(never()).save(any());

            and.then(error)
                .asInstanceOf(type(DeviceLimitReachedException.class))
                .extracting(DeviceLimitReachedException::getLimit,
                    DeviceLimitReachedException::getCustomerId)
                .containsExactly(DEVICE_LIMIT, customerId);
        }

        @Test
        void create_returnsUuidOfCreatedDevice_givenLimitNotReachedForGivenCustomer() {
            // given
            final var customerId = UUID.randomUUID();
            final var expected = UUID.randomUUID();
            final var newDevice = new Device();
            final var request = new CreateDeviceRequest(customerId.toString(), "", "");

            given(repository.countByCustomerId(customerId))
                .willReturn(RandomUtils.nextInt(0, DEVICE_LIMIT));
            given(mapper.toNewEntity(request))
                .willReturn(newDevice);
            given(repository.save(newDevice))
                .will(invocation -> invocation.getArgument(0, Device.class).setId(expected));

            // when
            final UUID actual = underTest.create(request);

            // then
            and.then(actual).isEqualTo(expected);
        }

        @Test
        void read_throws_givenDeviceNotFound() {
            // given
            final var id = UUID.randomUUID();

            given(repository.findById(id))
                .willReturn(Optional.empty());

            // when
            final var error = catchThrowable(() -> underTest.read(id.toString()));

            // then
            then(mapper).shouldHaveNoInteractions();
            and.then(error)
                .asInstanceOf(type(DeviceNotFoundException.class))
                .extracting(DeviceNotFoundException::getIdentifier)
                .isEqualTo(id);
        }

        @Test
        void read_returnsDeviceDto_givenDeviceFound() {
            // given
            final var id = UUID.randomUUID();
            final var customer = new Device()
                .setId(id);
            final var expected = new ReadDeviceResponse(id.toString(), "", "", "");

            given(repository.findById(id))
                .willReturn(Optional.of(customer));

            given(mapper.toDto(customer))
                .willReturn(expected);

            // when
            final ReadDeviceResponse actual = underTest.read(id.toString());

            // then
            and.then(actual).isEqualTo(expected);
        }

        @Test
        void update_throws_givenDeviceNotFound() {
            // given
            final var id = UUID.randomUUID();
            final var request = new UpdateDeviceRequest("some status", "some color");

            // when
            final var error = catchThrowable(() -> underTest.update(id.toString(), request));

            // then
            then(mapper).shouldHaveNoInteractions();
            and.then(error)
                .asInstanceOf(type(DeviceNotFoundException.class))
                .extracting(DeviceNotFoundException::getIdentifier)
                .isEqualTo(id);
        }

        @Test
        void update_mapsRequestToDevice_givenDeviceFound() {
            // given
            final var id = UUID.randomUUID();
            final var request = new UpdateDeviceRequest("some status", "some color");

            final var customer = new Device().setId(id);

            given(repository.findById(id))
                .willReturn(Optional.of(customer));

            // when
            underTest.update(id.toString(), request);

            // then
            then(mapper).should().toEntity(request, customer);
        }

        @Test
        void delete_throws_givenDeviceNotFound() {
            // given
            final var id = UUID.randomUUID();

            given(repository.findById(id))
                .willReturn(Optional.empty());

            // when
            final var error = catchThrowable(() -> underTest.delete(id.toString()));

            // then
            then(mapper).shouldHaveNoInteractions();
            and.then(error)
                .asInstanceOf(type(DeviceNotFoundException.class))
                .extracting(DeviceNotFoundException::getIdentifier)
                .isEqualTo(id);
        }

        @Test
        void delete_returnsWithoutThrowing_givenDeviceFound() {
            // given
            final var id = UUID.randomUUID();
            final var customer = new Device().setId(id);

            given(repository.findById(id))
                .willReturn(Optional.of(customer));

            // when/then
            thenNoException().isThrownBy(() -> underTest.delete(id.toString()));
        }
    }

    static class IllegalIds implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.<String>builder()
                .add(null)
                .add("")
                .add("  ")
                .add("not a UUID")
                .build()
                .map(Arguments::of);
        }
    }

    static class Update_IllegalArguments implements ArgumentsProvider {
        static final UpdateDeviceRequest validUpdate =
            new UpdateDeviceRequest("something", "something");

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.<Arguments>builder()
                .add(arguments(null, null))
                .add(arguments("", validUpdate))
                .add(arguments("  ", validUpdate))
                .add(arguments("not a UUID", validUpdate))
                .build();
        }
    }

}
