package io.github.finoid.testify.testcontainers.extensions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class RedisContainerExtensionTest {
    @RegisterExtension
    static final RedisContainerExtension REDIS = RedisContainerExtension.create();

    @Test
    void givenContainerExtension_whenTestRuns_thenContainerIsRunning() {
        Assertions.assertTrue(REDIS.isContainerRunning());
    }
}