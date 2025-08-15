package io.github.finoid.testify.snapshot;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.SnapshotVerifier;
import au.com.origin.snapshots.config.PropertyResolvingSnapshotConfig;
import au.com.origin.snapshots.exceptions.SnapshotMatchException;
import au.com.origin.snapshots.utils.ReflectionUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JUnit 5 extension that provides an instance of {@link Snapshotter} for parameter injection.
 */
public class SnapshotterExtension implements ParameterResolver, BeforeAllCallback, AfterAllCallback, BeforeTestExecutionCallback {
    private static final ExtensionContext.Namespace NS = ExtensionContext.Namespace.create("run-count");

    @SuppressWarnings("NullAway.Init")
    private SnapshotVerifier verifier;

    @Override
    public void beforeAll(final ExtensionContext context) {
        final Class<?> testClass = context.getTestClass()
            .orElseThrow(() -> new SnapshotMatchException("Unable to locate Test class"));

        final PropertyResolvingSnapshotConfig snapshotConfig = new PropertyResolvingSnapshotConfig();

        verifier = new SnapshotVerifier(snapshotConfig, testClass, false);
    }

    @Override
    public void afterAll(final ExtensionContext context) throws IllegalAccessException {
        final AtomicInteger testMethodsExecuted = context.getRoot()
            .getStore(NS)
            .getOrDefault("running", AtomicInteger.class, new AtomicInteger(0));

        // Brittle hack - upsource a fix or re-write SnapshotVerifier
        final Field field = ReflectionUtils.findFieldByPredicate(SnapshotVerifier.class, f -> f.getName().equalsIgnoreCase("failOnOrphans"))
            .orElseThrow(() -> new SnapshotMatchException("Unable to locate failOnOrphans field - please open a issue"));
        ReflectionUtils.makeAccessible(field);

        if (testMethodsExecuted.get() > 1) {
            field.set(verifier, true);
        } else {
            field.set(verifier, false);
        }

        verifier.validateSnapshots();
    }

    @Override
    public void beforeTestExecution(final ExtensionContext context) {
        final ExtensionContext.Store store = context.getRoot()
            .getStore(NS);

        final AtomicInteger counter = store.getOrComputeIfAbsent("running", k -> new AtomicInteger(), AtomicInteger.class);

        counter.incrementAndGet();
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
