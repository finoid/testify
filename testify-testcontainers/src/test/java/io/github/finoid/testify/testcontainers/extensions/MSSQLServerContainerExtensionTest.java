package io.github.finoid.testify.testcontainers.extensions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MSSQLServerContainerExtensionTest {
    @RegisterExtension
    static final MSSQLServerContainerExtension MSSQL = MSSQLServerContainerExtension.create();

    @Test
    void givenContainerExtension_whenTestRuns_thenContainerIsRunning() {
        Assertions.assertTrue(MSSQL.isContainerRunning());
    }
}