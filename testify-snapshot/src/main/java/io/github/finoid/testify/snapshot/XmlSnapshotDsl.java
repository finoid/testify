package io.github.finoid.testify.snapshot;

import au.com.origin.snapshots.Expect;
import io.github.finoid.testify.core.internal.Precondition;
import io.github.finoid.testify.snapshot.SnapshotDsl.ScenarioSnapshotDsl;

/**
 * A DSL for snapshotting XML objects with optional masking of fields.
 */
public class XmlSnapshotDsl extends ScenarioSnapshotDsl {
    @SuppressWarnings("UnusedVariable") // TODO (nw) temporary
    private final Expect expect;

    private XmlSnapshotDsl(final Expect expect) {
        this.expect = Precondition.nonNull(expect, "Expect must not be null");
    }

    /**
     * Factory method to create a {@link XmlSnapshotDsl} instance using the given {@link Expect}.
     *
     * @param expect the expect instance
     * @return a new {@code XmlSnapshotDsl}
     * @throws IllegalArgumentException if expect is null
     */
    public static XmlSnapshotDsl ofExpect(final Expect expect) {
        return new XmlSnapshotDsl(expect);
    }

    @Override
    public <T> void snapshot(T toBeSnapshotted) {
        throw new UnsupportedOperationException("XML snapshots are not yet supported"); // TODO (nw) support masking
    }
}
