package io.github.finoid.testify.spring.type;

import io.github.finoid.testify.snapshot.SnapshotterExtension;
import io.github.finoid.testify.spring.http.HttpAsserter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/**
 * Base test class for E2E-related tests, providing utility methods for HTTP assertions and component setup.
 *
 * <pre>{@code @Import(CustomEndToEndHttpTest.ExampleController.class)
 * class CustomEndToEndHttpTest extends EndToEndHttpTest {
 *     @Autowired
 *     private HttpAsserter asserter;
 *
 *     @Test
 *     void givenHttpRequest_whenRequestAndJsonResponse_thenSuccessfulJsonResponse() {
 *         var httpRequestSpec = RequestSpec.get("/v1/hello")
 *             .andExpect()
 *             .status(HttpStatus.OK)
 *             .responseOf(new TypeReference<User>() {});
 *
 *         var result = asserter.perform(httpRequestSpec);
 *
 *         Assertions.assertEquals(new User(1, "hello"), result.deserializedOrNull());
 *     }
 *
 *     @RestController
 *     static class ExampleController {
 *         @RequestMapping("/v1/hello")
 *         public User helloJson() {
 *             return new User(1, "hello");
 *         }
 *     }
 *
 *     record User(int id, String name) {}
 *
 *     @SpringBootApplication
 *     private static class CustomApplication {
 *         @SuppressWarnings("required.method.not.called")
 *         public static void main(final String[] args) {
 *             SpringApplication.run(CustomApplication.class, args);
 *         }
 *     }
 * }
 * }
 * </pre>
 */
@Tag("EndToEndHttpTest")
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection"})
@Execution(ExecutionMode.SAME_THREAD) // run sequentially
@ExtendWith(SnapshotterExtension.class)
@Import(EndToEndHttpTest.EndToEndHttpTestConfiguration.class)
public class EndToEndHttpTest {
    @Autowired
    @SuppressWarnings("UnusedVariable")
    protected HttpAsserter httpAsserter;

    @TestConfiguration
    public static class EndToEndHttpTestConfiguration {
        @Bean
        public HttpAsserter httpAsserter(final MockMvc mvc, final ObjectMapper objectMapper) {
            return HttpAsserter.ofMockMvcAndObjectMapper(mvc, objectMapper);
        }
    }
}
