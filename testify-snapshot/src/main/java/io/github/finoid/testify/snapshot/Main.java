package io.github.finoid.testify.snapshot;

//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.BooleanNode;
//import com.fasterxml.jackson.databind.node.DoubleNode;
//import com.fasterxml.jackson.databind.node.NullNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.Jackson3JsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.Jackson3MappingProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.Data;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Convert arbitrary object graph to a JsonNode, avoiding infinite recursion by tracking visited object identities.
     * Circular references are replaced with a marker (TextNode "[CIRCULAR_REF]" by default).
     */
    public static JsonNode toSafeJsonNode(Object obj) {
        return toSafeJsonNode(obj, new IdentityHashMap<>());
    }

    private static JsonNode toSafeJsonNode(Object obj, IdentityHashMap<Object, JsonNode> visited) {
        if (obj == null) return NullNode.instance;

        // primitives and common simple types
        if (obj instanceof String) return new StringNode((String) obj);
        if (obj instanceof Number) return new DoubleNode(((Number) obj).doubleValue());
        if (obj instanceof Boolean) return BooleanNode.valueOf((Boolean) obj);
        if (obj instanceof Instant) return new StringNode(obj.toString());
        if (obj instanceof Enum) return new StringNode(((Enum<?>) obj).name());

        // Avoid infinite recursion: check visited map
        if (visited.containsKey(obj)) {
            // you may return NullNode.instance or a marker object
            return new StringNode("[CIRCULAR_REF]");
        }

        // Mark as visited with a placeholder (we will replace with real node)
        visited.put(obj, NullNode.instance);

        // Collections / arrays
        if (obj instanceof Collection) {
            ArrayNode arrayNode = MAPPER.createArrayNode();
            visited.put(obj, arrayNode);
            for (Object item : (Collection<?>) obj) {
                arrayNode.add(toSafeJsonNode(item, visited));
            }
            return arrayNode;
        }

        if (obj.getClass().isArray()) {
            ArrayNode arrayNode = MAPPER.createArrayNode();
            visited.put(obj, arrayNode);
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                arrayNode.add(toSafeJsonNode(Array.get(obj, i), visited));
            }
            return arrayNode;
        }

        if (obj instanceof Map) {
            ObjectNode mapNode = MAPPER.createObjectNode();
            visited.put(obj, mapNode);
            Map<?, ?> map = (Map<?, ?>) obj;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                String key = String.valueOf(e.getKey());
                mapNode.set(key, toSafeJsonNode(e.getValue(), visited));
            }
            return mapNode;
        }

        // Fallback: treat as bean — use JavaBeans introspection to get properties
        ObjectNode node = MAPPER.createObjectNode();
        visited.put(obj, node);
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(obj.getClass(), Object.class).getPropertyDescriptors()) {
                String name = pd.getName();
                if (pd.getReadMethod() == null) continue;
                Object value;
                try {
                    value = pd.getReadMethod().invoke(obj);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    // if property can't be read, skip it
                    continue;
                }
                node.set(name, toSafeJsonNode(value, visited));
            }
        } catch (IntrospectionException e) {
            // If introspection fails, fall back to toString
            return new StringNode(obj.toString());
        }
        return node;
    }

    /**
     * Mask fields matching the provided JsonPath expressions.
     *
     * @param root The safe JsonNode representation of the object
     * @param jsonPaths list of JsonPath expressions to set to maskValue
     * @param maskValue value to set (e.g. "***REDACTED***")
     * @return masked JSON string
     */
    public static String maskWithJsonPath(JsonNode root, List<String> jsonPaths, Object maskValue) {
        Configuration conf = Configuration.builder()
            .jsonProvider(new Jackson3JsonNodeJsonProvider())
            .mappingProvider(new Jackson3MappingProvider())
            .build();

        // parse from JsonNode using the Jackson provider (no re-parsing from String needed)
        DocumentContext ctx = JsonPath.using(conf).parse(root);

        for (String p : jsonPaths) {
            try {
                ctx.set(p, maskValue);
            } catch (Exception ex) {
                // log or ignore if path not found; choice depends on desired behavior
            }
        }
        // return pretty JSON if you want:
        try {
            Object mapped = ctx.json();
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(mapped);
        } catch (Exception ex) {
            // fallback to raw
            return root.toString();
        }
    }

    // small demo
    public static void main(String[] args) {
        // Example object with circular references
        @Data
        class Node {
            public String id;
            public Node child;
            public String secret = "s3cr3t";
            Node(String id) { this.id = id; }
        }
        Node a = new Node("a");
        Node b = new Node("b");
        a.child = b;
        b.child = a; // circular

        JsonNode safe = toSafeJsonNode(a);
        System.out.println("Before masking:\n" + safe.toPrettyString());

        List<String> pathsToMask = Arrays.asList("$.secret", "$..secret"); // mask any secret field
        String masked = maskWithJsonPath(safe, pathsToMask, "***REDACTED***");
        System.out.println("After masking:\n" + masked);
    }
}

