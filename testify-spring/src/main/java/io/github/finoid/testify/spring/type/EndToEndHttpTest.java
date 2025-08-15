package io.github.finoid.testify.spring.type;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finoid.testify.snapshot.SnapshotterExtension;
import io.github.finoid.testify.spring.http.HttpAsserter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base test class for E2E-related tests, providing utility methods for HTTP assertions,
 * JSON snapshot testing, and component setup.
 */
@Tag("EndToEndHttpTest")
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection"})
@ExtendWith({SnapshotterExtension.class})
@Import(EndToEndHttpTest.EndToEndHttpTestConfiguration.class)
public class EndToEndHttpTest {
    @Autowired
    @SuppressWarnings("UnusedVariable")
    private HttpAsserter httpAsserter;

    @TestConfiguration
    public static class EndToEndHttpTestConfiguration {
        @Bean
        public HttpAsserter httpAsserter(final MockMvc mvc, final ObjectMapper objectMapper) {
            return HttpAsserter.ofMockMvcAndObjectMapper(mvc, objectMapper);
        }
    }
}
