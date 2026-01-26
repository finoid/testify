package io.github.finoid.testify.snapshot;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * A custom Jackson module that ensures collections are serialized in a **deterministic order**.
 * This module replaces the default Jackson serialization of {@link Collection} types
 * by converting them into sorted arrays before serialization. This helps create
 * consistent JSON snapshots, preventing variations due to unordered collections.
 * <p>
 * If sorting is not possible (e.g., due to non-comparable elements), the collection
 * will be serialized in its original order, and a debug log will be emitted.
 */
@Slf4j
public class DeterministicCollectionModule extends SimpleModule {
    @SuppressWarnings("this-escape")
    public DeterministicCollectionModule() {
        addSerializer(Collection.class, new CollectionSerializer());
    }

    /**
     * Collections get converted into a sorted Object[].  This then gets serialized using the default Array serializer.
     */
    private static class CollectionSerializer<T> extends ValueSerializer<Collection<T>> {
        /**
         * Serializes a {@link Collection} into a sorted {@code Object[]} for deterministic output.
         *
         * @param value the collection to be serialized
         * @param gen   the JSON generator
         * @param ctxt  the serialization context
         */
        @Override
        public void serialize(final Collection value, final tools.jackson.core.JsonGenerator gen, final SerializationContext ctxt)
            throws JacksonException {
            Object[] sorted = convert(value);

            if (value == null) {
                ctxt.getDefaultNullValueSerializer().serialize(null, gen, ctxt);
            } else {
                ctxt.findTypedValueSerializer(Object[].class, true).serialize(sorted, gen, ctxt);
            }
        }

        /**
         * Converts a collection into a sorted array, or an unsorted array if sorting fails.
         *
         * @param value the collection to be converted
         * @return a sorted array or an unsorted array if sorting fails
         */
        private Object[] convert(final Collection<?> value) {
            if (value == null || value.isEmpty()) {
                return Collections.emptyList().toArray();
            }

            try {
                return value.stream()
                    .filter(Objects::nonNull)
                    .sorted()
                    .toArray();
            } catch (ClassCastException ex) {
                log.warn(
                    "Unable to sort() collection - this may result in a non deterministic snapshot.\n"
                        + "Consider adding a custom serializer for this type via the JacksonSnapshotSerializer#configure() method.\n"
                        + ex.getMessage());
                return value.toArray();
            }
        }
    }
}
