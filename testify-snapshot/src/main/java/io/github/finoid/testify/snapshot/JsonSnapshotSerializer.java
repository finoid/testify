package io.github.finoid.testify.snapshot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import io.github.finoid.snapshots.Snapshot;
import io.github.finoid.snapshots.SnapshotSerializerContext;
import io.github.finoid.snapshots.exceptions.SnapshotExtensionException;
import io.github.finoid.snapshots.serializers.SerializerType;
import io.github.finoid.snapshots.serializers.SnapshotSerializer;
import io.github.finoid.testify.core.internal.Precondition;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

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

            final String body = objectMapper.writerWithDefaultPrettyPrinter()
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

    private static ObjectMapper createObjectMapper(final SimpleModule simpleModule) {
        final JsonMapper.Builder builder =
            JsonMapper.builder()
                .defaultPrettyPrinter(new SnapshotPrettyPrinter())
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .enable(DateTimeFeature.WRITE_DATES_WITH_ZONE_ID)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .findAndAddModules()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL))
                .changeDefaultVisibility(visibility ->
                    visibility.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        builder.addModule(simpleModule);
        builder.addModule(new DeterministicCollectionModule()); // TODO (nw) use the one from java-snapshot-testing

        return builder.build();
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
        private static final DefaultPrettyPrinter.Indenter LF_ONLY_INDENTER = new DefaultIndenter("  ", "\n");
        private static final Separators DEFAULT_SEPARATORS = Separators.createDefaultInstance().withRootSeparator("");

        public SnapshotPrettyPrinter() {
            this.indentArraysWith(LF_ONLY_INDENTER);
            this.indentObjectsWith(LF_ONLY_INDENTER);
            this.withSeparators(DEFAULT_SEPARATORS);
        }

        @Override
        public DefaultPrettyPrinter createInstance() {
            return new DefaultPrettyPrinter(this);
        }
    }
}
