package integration;

import static org.apache.commons.lang3.StringUtils.stripStart;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PATCH;

import java.net.URI;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

public interface RestIntegrationTest {
    int getPort();

    TestRestTemplate getRest();

    default URI urlOf(String resource) {
        final var string = "http://localhost:%d/%s".formatted(getPort(), stripStart(resource, "/"));
        return URI.create(string);
    }

    default <T> ResponseEntity<T> patchForEntity(URI uri, Object body, Class<T> responseType) {
        final HttpEntity<?> httpEntity = body instanceof HttpEntity<?> entity
            ? entity
            : new HttpEntity<>(body);

        return getRest().exchange(uri, PATCH, httpEntity, responseType);
    }

    default <T> ResponseEntity<T> deleteForEntity(URI uri, Class<T> responseType) {
        return getRest().exchange(uri, DELETE, null, responseType);
    }
}
