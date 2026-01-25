package io.github.finoid.testify.snapshot;

import io.github.finoid.snapshots.Expect;
import io.github.finoid.snapshots.junit5.SnapshotExtension;
import lombok.RequiredArgsConstructor;

/**
 * A convenience class that provides access to various snapshot DSLs.
 * <p>
 * This class is meant to be injected as a parameter in JUnit 5 tests, and it wraps a shared
 * {@link Expect} instance provided by the {@link SnapshotExtension}.
 */
@RequiredArgsConstructor
public class Snapshotter {
    private final Expect expect;

    /**
     * Creates a DSL for JSON-based snapshot testing.
     *
     * @return a configured {@link JsonSnapshotDsl} instance
     */
    public JsonSnapshotDsl json() {
        return JsonSnapshotDsl.ofExpect(expect);
    }

    /**
     * Creates a DSL for plain text snapshot testing.
     *
     * @return a configured {@link PlainSnapshotDsl} instance
     */
    public PlainSnapshotDsl plain() {
        return PlainSnapshotDsl.ofExpect(expect);
    }

    /**
     * Creates a DSL for base64 encoded text snapshot testing.
     *
     * @return a configured {@link Base64SnapshotDsl} instance
     */
    public Base64SnapshotDsl base64() {
        return Base64SnapshotDsl.ofExpect(expect);
    }

    /**
     * Creates a DSL for XML-based snapshot testing.
     *
     * @return a configured {@link XmlSnapshotDsl} instance
     */
    public XmlSnapshotDsl xml() {
        return XmlSnapshotDsl.ofExpect(expect);
    }
}
