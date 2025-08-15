package io.github.finoid.testify.testcontainers.extensions;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.github.finoid.testify.core.internal.Precondition;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

/**
 * A JUnit extension to manage a POSTGRES container for integration and e2e tests.
 *
 * <pre>{@code
 * @RegisterExtension
 * static final PostgresContainerExtension postgres = MSSQLServerContainerExtension.create(
 *     PostgresContainerExtension.Configuration.defaultConfiguration()
 *         .dockerImage(DockerImageName.parse("<docker repository proxy>/postgres:17-alpine")
 *             .asCompatibleSubstituteFor("postgres"))
 *         .reuse(true)
 *     );
 *  };
 * </pre>
 */
public class PostgresContainerExtension implements BeforeAllCallback, AfterAllCallback {
    private final Configuration configuration;
    @Nullable
    private volatile PostgreSQLContainer<?> container;

    private PostgresContainerExtension() {
        this(Configuration.defaultConfiguration());
    }

    private PostgresContainerExtension(final Configuration configuration) {
        this.configuration = Precondition.nonNull(configuration, "Configuration must not be null");

        configuration.validateSelf();
    }

    /**
     * Sets up the Postgres container before all tests are run.
     *
     * @param context The context in which the given test is executed.
     */
    @Override
    @SuppressWarnings("resource")
    public void beforeAll(final ExtensionContext context) {
        if (container != null && container.isRunning()) {
            return; // Already started
        }

        synchronized (this) {
            if (container == null) {
                this.container =
                    new PostgreSQLContainer<>(configuration.dockerImage)
                        .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                            HostConfig.newHostConfig()
                                .withPortBindings(
                                    new PortBinding(Ports.Binding.bindPort(configuration.hostPort), new ExposedPort(configuration.containerPort)))
                        ))
                        .withDatabaseName(configuration.databaseName)
                        .withReuse(true);
            }
            //noinspection DataFlowIssue
            container.start();
        }
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        // Stop only if not reusing, otherwise keep the container for performance purpose
        final PostgreSQLContainer<?> container = this.container;
        if (container != null && container.isRunning() && !configuration.reuse) {
            container.stop();
        }
    }

    /**
     * Retrieves the host address of the Postgres container.
     *
     * @return The host address of the container.
     * @throws IllegalStateException in case the container hasn't been created yet.
     */
    @SuppressWarnings("resource")
    public String getHost() {
        final PostgreSQLContainer<?> container = ensureInitialized();

        return container.getHost();
    }

    /**
     * Retrieves the port number exposed to the Postgres service inside the container.
     *
     * @return The exposed port number.
     * @throws IllegalStateException in case the container hasn't been created yet.
     */
    @SuppressWarnings("resource")
    public int getPort() {
        final PostgreSQLContainer<?> container = ensureInitialized();

        return container.getFirstMappedPort();
    }

    /**
     * Retrieves the jdbc url of the Postgres container.
     *
     * @return The jdbc url of the container.
     * @throws IllegalStateException in case the container hasn't been created yet.
     */
    @SuppressWarnings("resource")
    public String getJdbcUrl() {
        final PostgreSQLContainer<?> container = ensureInitialized();

        return container.getJdbcUrl();
    }

    /**
     * Retrieves the username used by the container.
     *
     * @return the username used by the container.
     * @throws IllegalStateException in case the container hasn't been created yet.
     */
    @SuppressWarnings("resource")
    public String getUsername() {
        final PostgreSQLContainer<?> container = ensureInitialized();

        return container.getUsername();
    }

    /**
     * Retrieves the password configured for the container.
     *
     * @return the password configured for the container.
     */
    @SuppressWarnings("resource")
    public String getPassword() {
        final PostgreSQLContainer<?> container = ensureInitialized();

        return container.getPassword();
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
            "spring.datasource.url", getJdbcUrl(),
            "spring.datasource.username", getUsername(),
            "spring.datasource.password", getPassword()
        );
    }

    /**
     * Factory method to create a new instance of PostgresContainerExtension with default configuration.
     *
     * @return A new instance of PostgresContainerExtension.
     */
    public static PostgresContainerExtension create() {
        return new PostgresContainerExtension(Configuration.defaultConfiguration());
    }

    /**
     * Factory method to create a new instance of PostgresContainerExtension with custom configuration.
     *
     * @return A new instance of PostgresContainerExtension.
     */
    public static PostgresContainerExtension create(final Configuration configuration) {
        return new PostgresContainerExtension(configuration);
    }

    private PostgreSQLContainer<?> ensureInitialized() {
        final PostgreSQLContainer<?> container = this.container;
        if (container == null) {
            throw new IllegalStateException("PostgreSQLContainer is not created yet. Did you register the extension and let it start?");
        }
        return container;
    }

    /**
     * Configuration class for PostgresContainerExtension. Allows setting custom configurations for the Postgres Docker container.
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Configuration {
        private static final int DEFAULT_HOST_PORT = 5433;
        private static final int DEFAULT_CONTAINER_PORT = 5432;

        private int hostPort = DEFAULT_HOST_PORT;
        private int containerPort = DEFAULT_CONTAINER_PORT;
        private String databaseName = "test";
        private String password = "Secret1234";

        private DockerImageName dockerImage = DockerImageName.parse("postgres:17-alpine");
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
         * Sets the password in the container.
         *
         * @param password The database password.
         * @return This Configuration object for chaining.
         * @throws IllegalArgumentException if the password is null
         */
        public Configuration password(final String password) {
            this.password = Precondition.nonNull(password, "Password must not be null.");

            return this;
        }

        /**
         * Sets the Docker image tag to be used for the Postgres container.
         *
         * @param dockerImage The Docker image
         * @return This Configuration object for chaining.
         */
        public Configuration dockerImage(final DockerImageName dockerImage) {
            this.dockerImage = Precondition.nonNull(dockerImage, "Docker image must not be null.");

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

        /**
         * Sets the database name to be used for the Postgres container.
         *
         * @param databaseName The database name.
         * @return This Configuration object for chaining.
         */
        public Configuration databaseName(final String databaseName) {
            this.databaseName = databaseName;

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
         * Returns the default configuration for the Postgres container.
         *
         * @return Default configuration.
         */
        public static Configuration defaultConfiguration() {
            return new Configuration();
        }
    }
}
