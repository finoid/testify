package io.github.finoid.testify.spring.type;

import io.github.finoid.testify.snapshot.SnapshotterExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base class for integration tests, testing utilities.
 */
@Tag("IntegrationTest")
@SpringBootTest
@ExtendWith({SnapshotterExtension.class})
public class IntegrationTest {

}
