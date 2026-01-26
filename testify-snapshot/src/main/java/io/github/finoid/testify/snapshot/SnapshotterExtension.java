package io.github.finoid.testify.snapshot;

import io.github.finoid.snapshots.Expect;
import io.github.finoid.snapshots.SnapshotVerifier;
import io.github.finoid.snapshots.config.PropertyResolvingSnapshotConfig;
import io.github.finoid.snapshots.exceptions.SnapshotMatchException;
import io.github.finoid.snapshots.utils.ReflectionUtils;
import lombok.Value;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JUnit 5 extension that provides an instance of {@link Snapshotter} for parameter injection.
 */
public class SnapshotterExtension implements ParameterResolver, BeforeAllCallback, AfterAllCallback, BeforeTestExecutionCallback {
    private static final ExtensionContext.Namespace NS = ExtensionContext.Namespace.create("run-state");
    private static final String KEY_STATE = "state";

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
        final State state = context.getRoot()
            .getStore(NS)
            .getOrDefault(KEY_STATE, State.class, new State());

        // Brittle hack - upsource a fix or re-write SnapshotVerifier
        final Field field = ReflectionUtils.findFieldByPredicate(SnapshotVerifier.class, f -> f.getName().equalsIgnoreCase("failOnOrphans"))
            .orElseThrow(() -> new SnapshotMatchException("Unable to locate failOnOrphans field - please open a issue"));
        ReflectionUtils.makeAccessible(field);

        if (state.getExecutedMethodCount() > 1) {
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

        final State state = store.computeIfAbsent(KEY_STATE, k -> new State(), State.class);

        final String commonMethodName = context.getTestMethod()
            .map(Method::getName)
            .orElse("<unknown>");

        state.addExecutedTestMethod(commonMethodName);
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

    @Value
    private static class State {
        Set<String> executedTestMethods = ConcurrentHashMap.newKeySet();

        public void addExecutedTestMethod(final String testMethod) {
            executedTestMethods.add(testMethod);
        }

        public int getExecutedMethodCount() {
            return executedTestMethods.size();
        }
    }
}
