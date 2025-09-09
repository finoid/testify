package io.github.finoid.testify.spring.type;

import io.github.finoid.testify.snapshot.SnapshotterExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base class for integration tests, providing testing utilities.
 *
 * <pre>{@code
 * @ContextConfiguration(classes = CustomIntegrationTest.CustomTestConfiguration.class) // To prevent the whole application context from loading
 * class CustomIntegrationTest extends IntegrationTest {
 *     @Autowired
 *     private String hello;
 *
 *     @Test
 *     void givenPartOfContext_whenLoadingApplicationContext_thenExpectedBeanLoaded() {
 *         Assertions.assertEquals("Hello World!", hello);
 *     }
 *
 *     @TestConfiguration
 *     static class CustomTestConfiguration {
 *         @Bean
 *         public String hello() {
 *             return "Hello World!";
 *         }
 *     }
 * }
 * }
 * </pre>
 */
@Tag("IntegrationTest")
@Execution(ExecutionMode.SAME_THREAD) // run sequentially
@SpringBootTest
@ExtendWith(SnapshotterExtension.class)
public class IntegrationTest {

}
