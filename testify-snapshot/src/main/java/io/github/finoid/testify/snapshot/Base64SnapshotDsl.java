package io.github.finoid.testify.snapshot;

import io.github.finoid.snapshots.Expect;
import io.github.finoid.snapshots.serializers.v1.Base64SnapshotSerializer;
import io.github.finoid.testify.core.internal.Precondition;
import io.github.finoid.testify.snapshot.SnapshotDsl.ScenarioSnapshotDsl;

public class Base64SnapshotDsl extends ScenarioSnapshotDsl {
    private final Expect expect;

    public Base64SnapshotDsl(final Expect expect) {
        this.expect = Precondition.nonNull(expect, "Expect must not be null");
    }

    /**
     * Factory method to create a {@link Base64SnapshotDsl} instance using the given {@link Expect}.
     *
     * @param expect the expect instance
     * @return a new {@code Base64SnapshotDsl}
     * @throws IllegalArgumentException if expect is null
     */
    public static Base64SnapshotDsl ofExpect(final Expect expect) {
        return new Base64SnapshotDsl(expect);
    }

    @Override
    public <T> void snapshot(final T toBeSnapshotted) {
        expect.serializer(new Base64SnapshotSerializer()) // TODO (nw) option to pass a simple module?
            .scenario(scenario)
            .toMatchSnapshot(toBeSnapshotted);
    }
}
