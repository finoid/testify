package io.github.finoid.testify.snapshot;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.SnapshotVerifier;
import au.com.origin.snapshots.config.PropertyResolvingSnapshotConfig;
import au.com.origin.snapshots.exceptions.SnapshotMatchException;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * JUnit 5 extension that provides an instance of {@link Snapshotter} for parameter injection.
 */
public class SnapshotterExtension implements ParameterResolver, BeforeAllCallback {
    @SuppressWarnings("NullAway.Init")
    private SnapshotVerifier verifier;

    @Override
    public void beforeAll(final ExtensionContext context) {
        final Class<?> testClass = context.getTestClass()
            .orElseThrow(() -> new SnapshotMatchException("Unable to locate Test class"));

        final PropertyResolvingSnapshotConfig snapshotConfig = new PropertyResolvingSnapshotConfig();

        verifier = new SnapshotVerifier(snapshotConfig, testClass, true);
    }

    /**
     * Determines if the {@link Snapshotter} should be injected into the test method.
     */
    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == Snapshotter.class;
    }

    /**
     * Resolves and injects the {@link Snapshotter} instance for the current test method.
     */
    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) throws ParameterResolutionException {
        final Expect expect = Expect.of(verifier, extensionContext.getTestMethod()
            .orElseThrow(() -> new SnapshotMatchException("Unable to locate test method")));

        return new Snapshotter(expect);
    }
}
