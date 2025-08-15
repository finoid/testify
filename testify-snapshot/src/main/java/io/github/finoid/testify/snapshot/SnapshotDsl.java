package io.github.finoid.testify.snapshot;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public interface SnapshotDsl {
    /**
     * Captures or verifies a snapshot of the given object.
     *
     * @param toBeSnapshotted the object to snapshot
     * @param <T>             the type of the object
     */
    <T> void snapshot(final T toBeSnapshotted);

    /**
     * A base implementation that supports scenario.
     */
    abstract class ScenarioSnapshotDsl implements SnapshotDsl {
        @Nullable
        protected String scenario;

        /**
         * Configure a specified test scenario.
         * This is useful when running multiple variations of the same test case and needing distinct snapshots for each scenario.
         *
         * @param scenario the test scenario
         * @return this instance for fluent chaining
         */
        public SnapshotDsl withScenario(@Nullable final String scenario) {
            this.scenario = scenario;

            return this;
        }
    }

    /**
     * A base implementation that supports field masking.
     */
    abstract class MaskingSnapshotDsl extends ScenarioSnapshotDsl {
        protected List<String> maskedFieldPaths = Collections.emptyList();

        /**
         * Configures a single field to be masked.
         *
         * @param fieldPath the field path to mask
         * @return this instance for fluent chaining
         */
        public MaskingSnapshotDsl withMaskedField(final String fieldPath) {
            this.maskedFieldPaths = List.of(fieldPath);

            return this;
        }

        /**
         * Configures multiple fields to be masked.
         *
         * @param fieldPaths the list of field paths to mask
         * @return this instance for fluent chaining
         */
        public MaskingSnapshotDsl withMaskedFields(final List<String> fieldPaths) {
            this.maskedFieldPaths = List.copyOf(fieldPaths);

            return this;
        }
    }
}
