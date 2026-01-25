package io.github.finoid.testify.snapshot;

import io.github.finoid.snapshots.SnapshotVerifier;
import io.github.finoid.snapshots.exceptions.SnapshotMatchException;
import io.github.finoid.testify.core.type.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Optional;

class SnapshotterExtensionUnitTest extends UnitTest {
    @Mock
    private SnapshotVerifier verifier;
    @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private ParameterContext parameterContext;
    @Mock
    private ExtensionContext extensionContext;

    @InjectMocks
    private SnapshotterExtension unit;

    @Test
    void givenSnapshotterParameter_whenSupportsParameter_thenTrueReturned() {
        Mockito.when(parameterContext.getParameter().getType())
            .thenAnswer(it -> Snapshotter.class);

        Assertions.assertTrue(unit.supportsParameter(parameterContext, null));
    }

    @Test
    void givenNonExpectedParameter_whenSupportsParameter_thenFalseReturned() {
        Mockito.when(parameterContext.getParameter().getType())
            .thenAnswer(it -> String.class);

        Assertions.assertFalse(unit.supportsParameter(parameterContext, null));
    }

    @Test
    void givenSnapshotterParameter_whenResolveParameter_thenSnapshotterReturned() {
        var mockedMethod = Mockito.mock(Method.class);

        Mockito.when(extensionContext.getTestMethod())
            .thenReturn(Optional.of(mockedMethod));

        var resolvedParameter = unit.resolveParameter(parameterContext, extensionContext);

        Assertions.assertNotNull(resolvedParameter);
        Assertions.assertInstanceOf(Snapshotter.class, resolvedParameter);
    }

    @Test
    void givenNoTestMethod_whenResolveParameter_thenSnapshotMatchExceptionThrown() {
        Mockito.when(extensionContext.getTestMethod())
            .thenReturn(Optional.empty());

        Assertions.assertThrows(SnapshotMatchException.class, () -> unit.resolveParameter(parameterContext, extensionContext));
    }
}