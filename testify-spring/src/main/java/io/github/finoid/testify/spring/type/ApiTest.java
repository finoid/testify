package io.github.finoid.testify.spring.type;

import io.github.finoid.testify.spring.http.HttpAsserter.HttpAsserterDsl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tools.jackson.databind.ObjectMapper;

/**
 * Base test class for API-related tests, providing utility methods for HTTP assertions and component setup.
 *
 * <pre>{@code
 * class CustomApiTest extends ApiTest {
 *     @Autowired
 *     private HttpAsserter.HttpAsserterDsl dsl;
 *
 *     @Test
 *     void givenHttpRequest_whenRequestAndJsonResponse_thenSuccessfulJsonResponse() {
 *         var asserter = dsl.controller(new ExampleController())
 *             .toHttpAsserter();
 *
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
 *     record User(int id, String name) {
 *     }
 * }
 * }</pre>
 */
@Tag("ApiTest")
@AutoConfigureJsonTesters
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection"})
@Execution(ExecutionMode.CONCURRENT) // run in parallel
@ExtendWith({SpringExtension.class})
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
