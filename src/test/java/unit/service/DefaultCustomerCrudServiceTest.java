package unit.service;

import static org.assertj.core.api.BDDAssertions.and;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.jacopocav.customercare.component.CustomerMapper;
import io.jacopocav.customercare.dto.CreateCustomerRequest;
import io.jacopocav.customercare.dto.ReadCustomerResponse;
import io.jacopocav.customercare.dto.UpdateCustomerRequest;
import io.jacopocav.customercare.error.ResourceNotFoundException;
import io.jacopocav.customercare.model.Customer;
import io.jacopocav.customercare.repository.CustomerRepository;
import io.jacopocav.customercare.service.DefaultCustomerCrudService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("AccessStaticViaInstance")
class DefaultCustomerCrudServiceTest {
    @Mock CustomerMapper mapper;
    @Mock CustomerRepository repository;

    DefaultCustomerCrudService underTest;

    @BeforeEach
    void setUp() {
        underTest = new DefaultCustomerCrudService(mapper, repository);
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
        @ArgumentsSource(Read_IllegalArguments.class)
        void read_throws_givenIllegalId(String illegalId) {
            // when
            final var error = catchThrowable(() -> underTest.read(illegalId));

            // then
            and.then(error).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ArgumentsSource(Update_IllegalArguments.class)
        void update_throws_givenIllegalArguments(String id, UpdateCustomerRequest request) {
            // when
            final var error = catchThrowable(() -> underTest.update(id, request));

            // then
            and.then(error).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class LegalArgumentsTest {
        @Test
        void create_returnsUuidOfCreatedCustomer() {
            // given
            final var expected = UUID.randomUUID();
            final var newCustomer = new Customer();
            final var request = new CreateCustomerRequest("", "", "", "");

            given(mapper.toNewEntity(request))
                .willReturn(newCustomer);
            given(repository.save(newCustomer))
                .will(invocation -> invocation.getArgument(0, Customer.class).setId(expected));

            // when
            final UUID actual = underTest.create(request);

            // then
            and.then(actual).isEqualTo(expected);
        }

        @Test
        void read_throws_givenCustomerNotFound() {
            // given
            final var id = UUID.randomUUID();

            given(repository.findById(id))
                .willReturn(Optional.empty());

            // when
            final var error = catchThrowable(() -> underTest.read(id.toString()));

            // then
            then(mapper).shouldHaveNoInteractions();
            and.then(error)
                .asInstanceOf(type(ResourceNotFoundException.class))
                .extracting(ResourceNotFoundException::getIdentifier)
                .isEqualTo(id);
        }

        @Test
        void read_returnsCustomerDto_givenCustomerFound() {
            // given
            final var id = UUID.randomUUID();
            final var customer = new Customer()
                .setId(id);
            final var expected = new ReadCustomerResponse(id.toString(), "", "", "", "");

            given(repository.findById(id))
                .willReturn(Optional.of(customer));

            given(mapper.toDto(customer))
                .willReturn(expected);

            // when
            final ReadCustomerResponse actual = underTest.read(id.toString());

            // then
            and.then(actual).isEqualTo(expected);
        }

        @Test
        void update_throws_givenCustomerNotFound() {
            // given
            final var id = UUID.randomUUID();
            final var request = new UpdateCustomerRequest("some address");

            // when
            final var error = catchThrowable(() -> underTest.update(id.toString(), request));

            // then
            then(mapper).shouldHaveNoInteractions();
            and.then(error)
                .asInstanceOf(type(ResourceNotFoundException.class))
                .extracting(ResourceNotFoundException::getIdentifier)
                .isEqualTo(id);
        }

        @Test
        void update_mapsRequestToCustomer_givenCustomerFound() {
            // given
            final var id = UUID.randomUUID();
            final var request = new UpdateCustomerRequest("some address");

            final var customer = new Customer().setId(id);

            given(repository.findById(id))
                .willReturn(Optional.of(customer));

            // when
            underTest.update(id.toString(), request);

            // then
            then(mapper).should().toEntity(request, customer);
        }
    }

    static class Read_IllegalArguments implements ArgumentsProvider {
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
        static final UpdateCustomerRequest validUpdate = new UpdateCustomerRequest("something");

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
