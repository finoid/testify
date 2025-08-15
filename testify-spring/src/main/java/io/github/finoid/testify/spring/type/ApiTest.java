package io.github.finoid.testify.spring.type;

import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finoid.testify.spring.http.HttpAsserter.HttpAsserterDsl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Base test class for API-related tests, providing utility methods for HTTP assertions and component setup.
 */
@Tag("ApiTest")
@AutoConfigureJsonTesters
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection"})
@ExtendWith({SpringExtension.class, SnapshotExtension.class})
@Import(ApiTest.ApiTestConfiguration.class)
public class ApiTest {
    @TestConfiguration
    public static class ApiTestConfiguration {
        @Bean
        public HttpAsserterDsl httpAsserterDsl(final ObjectMapper objectMapper) {
            return HttpAsserterDsl.defaultConfiguration()
                .objectMapper(objectMapper);
        }
    }
}
