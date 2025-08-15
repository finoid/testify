package io.github.finoid.testify.snapshot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SnapshotterExtension.class)
class SnapshotterExtensionTest {
    @Test
    void givenSnapshotter_whenUserSnapshottedAsJson_thenSnapshotCreated(Snapshotter snapshotter) {
        snapshotter.json()
            .snapshot(new User(1, "John"));
    }

    @Test
    void givenSnapshotter_whenUserSnapshottedAsPlain_thenSnapshotCreated(Snapshotter snapshotter) {
        snapshotter.plain()
            .snapshot(new User(1, "John"));
    }

    @Test
    void givenSnapshotter_whenUserSnapshottedAsBase64_thenSnapshotCreated(Snapshotter snapshotter) {
        snapshotter.base64()
            .snapshot(new User(1, "John"));
    }

    record User(int id, String name) {}
}