package io.github.finoid.testify.snapshot;

import io.github.finoid.snapshots.Expect;
import io.github.finoid.snapshots.serializers.v1.ToStringSnapshotSerializer;
import io.github.finoid.testify.core.internal.Precondition;
import io.github.finoid.testify.snapshot.SnapshotDsl.ScenarioSnapshotDsl;

/**
 * A DSL for snapshotting plain objects to string.
 */
public class PlainSnapshotDsl extends ScenarioSnapshotDsl {
    private final Expect expect;

    private PlainSnapshotDsl(final Expect expect) {
        this.expect = Precondition.nonNull(expect, "Expect must not be null");
    }

    /**
     * Factory method to create a {@link PlainSnapshotDsl} instance using the given {@link Expect}.
     *
     * @param expect the expect instance
     * @return a new {@code PlainSnapshotDsl}
     * @throws IllegalArgumentException if expect is null
     */
    public static PlainSnapshotDsl ofExpect(final Expect expect) {
        return new PlainSnapshotDsl(expect);
    }

    @Override
    @SuppressWarnings("NullAway")
    public <T> void snapshot(final T toBeSnapshotted) {
        expect.serializer(new ToStringSnapshotSerializer()) // TODO (nw) option to pass simple module?
            .scenario(scenario)
            .toMatchSnapshot(toBeSnapshotted);
    }
}
