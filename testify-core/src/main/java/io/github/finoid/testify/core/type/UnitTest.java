package io.github.finoid.testify.core.type;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Base test class for unit-related tests that run in parallel.o
 */
@Tag("UnitTest")
@Execution(ExecutionMode.CONCURRENT) // run in parallel
@ExtendWith({MockitoExtension.class})
public class UnitTest {

}
