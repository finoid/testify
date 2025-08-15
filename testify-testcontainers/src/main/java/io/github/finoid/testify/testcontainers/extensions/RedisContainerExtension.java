package io.github.finoid.testify.testcontainers.extensions;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.redis.testcontainers.RedisContainer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

/**
 * A JUnit extension to manage a Redis container for integration and e2e tests.
 *
 * <pre>{@code
 * @RegisterExtension
 * static final RedisContainerExtension REDIS = RedisContainerExtension.create(
 *    RedisContainerExtension.Configuration.defaultConfiguration()
 *        .dockerImage(DockerImageName.parse("<docker repository proxy>/redis-7:9.3")
 *            .asCompatibleSubstituteFor("redis"))
 *        .reuse(true)
 *    );
 * };
 * }</pre>
 */
public class RedisContainerExtension implements BeforeAllCallback, AfterAllCallback {
    private final Configuration configuration;
    @Nullable
    private RedisContainer container;

    private RedisContainerExtension() {
        this(Configuration.defaultConfiguration());
    }

    private RedisContainerExtension(final Configuration configuration) {
        this.configuration = configuration;

        configuration.validateSelf();
    }

    /**
     * Sets up the Redis container before all tests are run.
     *
     * @param context The context in which the given test is executed.
     */
    @Override
    public void beforeAll(final ExtensionContext context) {
        if (container != null && container.isRunning()) {
            return; // Already started
        }

        synchronized (this) {
            if (container == null) {
                this.container =
                    new RedisContainer(configuration.dockerImage)
                        .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                            HostConfig.newHostConfig()
                                .withPortBindings(
                                    new PortBinding(Ports.Binding.bindPort(configuration.hostPort), new ExposedPort(configuration.containerPort)))
                        ))
                        .withReuse(true);
            }
            //noinspection DataFlowIssue
            container.start();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        // Stop only if not reusing, otherwise keep the container for performance purpose
        final RedisContainer container = this.container;
        if (container != null && container.isRunning() && !configuration.reuse) {
            container.stop();
        }
    }

    /**
     * Retrieves the host address of the Redis container.
     *
     * @return The host address of the container.
     * @throws IllegalStateException in case the container hasn't been created yet.
     */
    @SuppressWarnings("resource")
    public String getHost() {
        final RedisContainer container = ensureInitialized();

        return container.getHost();
    }

    /**
     * Retrieves the port number exposed to the Redis service inside the container.
     *
     * @return The exposed port number.
     * @throws IllegalStateException in case the container hasn't been created yet.
     */
    @SuppressWarnings("resource")
    public int getPort() {
        final RedisContainer container = ensureInitialized();

        return container.getFirstMappedPort();
    }

    /**
     * Returns {@code true} if the container has been initialized, false otherwise.
     *
     * @return {@code true} if the container has been initialized, false otherwise.
     */
    public boolean isContainerInitialized() {
        return container != null;
    }

    /**
     * Returns {@code true} if the container is running, false otherwise.
     *
     * @return {@code true} if the container is running, false otherwise.
     */
    public boolean isContainerRunning() {
        return container != null && container.isRunning();
    }

    /**
     * Convenience for Spring-style property injection.
     */
    public Map<String, String> toSpringDatasourceProperties() {
        return Map.of(
            "spring.data.redis.host", getHost(),
            "spring.data.redis.port", getPort() + ""
        );
    }

    private RedisContainer ensureInitialized() {
        final RedisContainer container = this.container;
        if (container == null) {
            throw new IllegalStateException("RedisContainer is not created yet. Did you register the extension and let it start?");
        }
        return container;
    }

    /**
     * Factory method to create a new instance of RedisContainerExtension with default configuration.
     *
     * @return A new instance of RedisContainerExtension.
     */
    public static RedisContainerExtension create() {
        return new RedisContainerExtension(Configuration.defaultConfiguration());
    }

    /**
     * Factory method to create a new instance of RedisContainerExtension with custom configuration.
     *
     * @return A new instance of RedisContainerExtension.
     */
    public static RedisContainerExtension create(final Configuration configuration) {
        return new RedisContainerExtension(configuration);
    }

    /**
     * Configuration class for RedisContainerExtension. Allows setting custom configurations for the Redis Docker container.
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Configuration {
        private static final int DEFAULT_HOST_PORT = 6380;
        private static final int DEFAULT_CONTAINER_PORT = 6379;

        private int hostPort = DEFAULT_HOST_PORT;
        private int containerPort = DEFAULT_CONTAINER_PORT;

        private DockerImageName dockerImage = DockerImageName.parse("redis:7.4.5");
        private boolean reuse = true;

        /**
         * Sets the port on the host to which the container's port will be bound.
         *
         * @param hostPort The host port to bind.
         * @return This Configuration object for chaining.
         */
        public Configuration hostPort(final int hostPort) {
            this.hostPort = hostPort;

            return this;
        }

        /**
         * Sets the port to be exposed from the container.
         *
         * @param containerPort The container port to expose.
         * @return This Configuration object for chaining.
         */
        public Configuration containerPort(final int containerPort) {
            this.containerPort = containerPort;

            return this;
        }

        /**
         * Sets the Docker image to be used for the Redis container.
         *
         * @param dockerImage The Docker image
         * @return This Configuration object for chaining.
         */
        public Configuration dockerTag(final DockerImageName dockerImage) {
            this.dockerImage = dockerImage;

            return this;
        }

        /**
         * Sets whether the container should be reused between tests runs.
         *
         * @param reuse whether the container should be reused.
         * @return This Configuration object for chaining.
         */
        public Configuration reuse(final boolean reuse) {
            this.reuse = reuse;

            return this;
        }

        private void validateSelf() {
            if (containerPort <= 0 || containerPort > 65535) {
                throw new IllegalArgumentException("containerPort must be in 1..65535");
            }
            if (hostPort <= 0 || hostPort > 65535) {
                throw new IllegalArgumentException("hostPort must be in 1..65535");
            }
        }

        /**
         * Returns the default configuration for the Redis container.
         *
         * @return Default configuration.
         */
        public static Configuration defaultConfiguration() {
            return new Configuration();
        }
    }
}
