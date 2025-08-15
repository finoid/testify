package io.github.finoid.testify.testcontainers.extensions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class PostgresContainerExtensionTest {
    @RegisterExtension
    static final PostgresContainerExtension POSTGRES = PostgresContainerExtension.create();

    @Test
    void givenContainerExtension_whenTestRuns_thenContainerIsRunning() {
        Assertions.assertTrue(POSTGRES.isContainerRunning());
    }
}