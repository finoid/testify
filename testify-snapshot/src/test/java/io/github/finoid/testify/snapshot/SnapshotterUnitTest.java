package io.github.finoid.testify.snapshot;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.SnapshotVerifier;
import au.com.origin.snapshots.config.PropertyResolvingSnapshotConfig;
import au.com.origin.snapshots.exceptions.SnapshotMatchException;
import io.github.finoid.testify.core.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

class SnapshotterUnitTest extends UnitTest {
    @Test
    void givenRecordInstanceToBeSerialized_whenJsonSnapshot_thenSuccessfulJsonSnapshotTaken(TestInfo testInfo) {
        var snapshotter = givenSnapshotVerifier(testInfo);

        snapshotter.json()
            .snapshot(new User(1, "John"));
    }

    @Test
    void givenRecordInstanceToBeSerialized_whenPlainSnapshot_thenSuccessfulPlainSnapshotTaken(TestInfo testInfo) {
        var snapshotter = givenSnapshotVerifier(testInfo);

        snapshotter.plain()
            .snapshot(new User(1, "John"));
    }

    @Test
    void givenRecordInstanceToBeSerialized_whenBase64Snapshot_thenSuccessfulBase64SnapshotTaken(TestInfo testInfo) {
        var snapshotter = givenSnapshotVerifier(testInfo);

        snapshotter.base64()
            .snapshot(new User(1, "John"));
    }

    @Test
    void xml() {
        // TODO (nw) to be implemented
    }

    private Snapshotter givenSnapshotVerifier(final TestInfo testInfo) {
        final SnapshotVerifier verifier = new SnapshotVerifier(new PropertyResolvingSnapshotConfig(), getClass(), true);

        final Expect expect = Expect.of(verifier, testInfo.getTestMethod()
            .orElseThrow(() -> new SnapshotMatchException("Unable to locate test method")));

        return new Snapshotter(expect);
    }

    record User(int id, String name) {
    }
}