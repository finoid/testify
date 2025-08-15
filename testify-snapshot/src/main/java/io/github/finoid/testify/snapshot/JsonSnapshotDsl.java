package io.github.finoid.testify.snapshot;

import au.com.origin.snapshots.Expect;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.finoid.testify.core.internal.Precondition;
import io.github.finoid.testify.snapshot.SnapshotDsl.MaskingSnapshotDsl;

/**
 * A DSL for snapshotting JSON objects with optional masking of fields.
 */
public class JsonSnapshotDsl extends MaskingSnapshotDsl {
    private final Expect expect;

    private JsonSnapshotDsl(final Expect expect) {
        this.expect = Precondition.nonNull(expect, "Expect must not be null");
    }

    /**
     * Factory method to create a {@link JsonSnapshotDsl} instance using the given {@link Expect}.
     *
     * @param expect the snapshot Expect instance
     * @return a new {@code JsonSnapshotDsl}
     * @throws IllegalArgumentException if expect is null
     */
    public static JsonSnapshotDsl ofExpect(final Expect expect) {
        return new JsonSnapshotDsl(expect);
    }

    @Override
    public <T> void snapshot(final T toBeSnapshotted) {
        expect.serializer(new JsonSnapshotSerializer(maskedFieldPaths, new SimpleModule())) // TODO (nw) option to pass a simple module?
            .scenario(scenario)
            .toMatchSnapshot(toBeSnapshotted);
    }
}
