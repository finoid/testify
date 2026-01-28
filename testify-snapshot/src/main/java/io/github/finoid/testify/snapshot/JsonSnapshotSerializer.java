package io.github.finoid.testify.snapshot;

import io.github.finoid.snapshots.Snapshot;
import io.github.finoid.snapshots.SnapshotSerializerContext;
import io.github.finoid.snapshots.exceptions.SnapshotExtensionException;
import io.github.finoid.snapshots.serializers.SerializerType;
import io.github.finoid.snapshots.serializers.SnapshotSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import io.github.finoid.testify.core.internal.Precondition;

import java.util.Collections;
import java.util.List;

/**
 * A custom JSON serializer for snapshot testing using the {@link SnapshotSerializer} interface.
 * <p>
 * This serializer integrates with Jackson to generate JSON-formatted snapshots, ensuring
 * deterministic serialization by:
 * <ul>
 *     <li>Sorting object properties alphabetically</li>
 *     <li>Ordering map entries by keys</li>
 *     <li>Applying consistent formatting using a custom pretty printer</li>
 * </ul>
 * Additionally, it supports field masking for sensitive data.
 * </p>
 */
public class JsonSnapshotSerializer implements SnapshotSerializer {
    private final List<String> maskedFieldPaths;
    private final ObjectMapper objectMapper;
    private final PrettyPrinter prettyPrinter = new SnapshotPrettyPrinter();

    /**
     * Creates a new {@code JsonSnapshotSerializer} with a list of masked fields.
     *
     * @param maskedFieldPaths A list of field names to mask in the serialized JSON output.
     */
    public JsonSnapshotSerializer(final List<String> maskedFieldPaths) {
        this(maskedFieldPaths, new SimpleModule());
    }

    /**
     * Creates a new {@code JsonSnapshotSerializer} with field masking and a custom Jackson module.
     *
     * @param maskedFieldPaths A list of field names to mask in the serialized JSON output.
     * @param simpleModule     A custom {@link SimpleModule} for additional Jackson configuration.
     * @throws IllegalArgumentException if the maskedFieldPaths or simpleModule is null.
     */
    public JsonSnapshotSerializer(final List<String> maskedFieldPaths, final SimpleModule simpleModule) {
        this.maskedFieldPaths = Precondition.nonNull(List.copyOf(maskedFieldPaths));
        this.objectMapper = Precondition.nonNull(createObjectMapper(simpleModule));
    }

    /**
     * Serializes the given object into a JSON snapshot.
     *
     * @param object The object to be serialized.
     * @param gen    The {@link SnapshotSerializerContext} that generates the snapshot.
     * @return A {@link Snapshot} containing the serialized JSON representation.
     * @throws SnapshotExtensionException if JSON serialization fails.
     */
    @Override
    public Snapshot apply(final Object object, final SnapshotSerializerContext gen) {
        try {
            final List<?> objects = Collections.singletonList(object);

            final String body = objectMapper.writer(this.prettyPrinter)
                .writeValueAsString(objects);

            final DocumentContext documentContext = JsonPath.parse(body);

            // TODO (nw) mask whole or parts of the values
            // TODO (nw) refactor squiggly and use instead of json path?
            // TODO (nw) option to pass a custom objectmapper

            maskedFieldPaths.forEach(it -> documentContext.set(it, "***MASKED***"));

            final String masked = documentContext.jsonString();

            return gen.toSnapshot(JsonFormatter.prettyPrint(masked));
        } catch (final InvalidPathException e) {
            throw new SnapshotExtensionException("Json snapshotting failed. Invalid mask field paths, please verify the paths. Paths: " + maskedFieldPaths, e);
        } catch (final JsonPathException e) {
            throw new SnapshotExtensionException("Json snapshotting failed. Couldn't mask field paths. Cause: " + e.getMessage(), e);
        } catch (final Exception e) {
            throw new SnapshotExtensionException("Json snapshotting failed. Cause: " + e.getMessage(), e);
        }
    }

    @Override
    public String getOutputFormat() {
        return SerializerType.JSON.name();
    }

    @SuppressWarnings("deprecation")
    private static ObjectMapper createObjectMapper(final SimpleModule simpleModule) {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        mapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
        mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        mapper.findAndRegisterModules();
        mapper.registerModule(simpleModule);
        mapper.registerModule(new DeterministicCollectionModule());

        mapper.setVisibility(
            mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
        );

        return mapper;
    }

    /**
     * Custom pretty printer for consistent JSON formatting in snapshots.
     * <p>
     * This printer:
     * <ul>
     *     <li>Uses two-space indentation for objects and arrays</li>
     *     <li>Formats object field-value separators with spaces</li>
     * </ul>
     * </p>
     */
    private static class SnapshotPrettyPrinter extends DefaultPrettyPrinter {
        public SnapshotPrettyPrinter() {
            final Indenter lfOnlyIndenter = new DefaultIndenter("  ", "\n");
            this.indentArraysWith(lfOnlyIndenter);
            this.indentObjectsWith(lfOnlyIndenter);

            this._objectFieldValueSeparatorWithSpaces =
                this._separators.getObjectFieldValueSeparator() + " ";
        }

        @Override
        public DefaultPrettyPrinter createInstance() {
            return new DefaultPrettyPrinter(this);
        }
    }
}
