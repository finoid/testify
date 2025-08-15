package io.github.finoid.testify.spring.http.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

@UtilityClass
public class WireMockUtils {
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    /**
     * Stubs a JSON response using a preconfigured {@link MappingBuilder}.
     * Sets status, {@code Content-Type: application/json; charset=UTF-8}, and serves the body via {@code bodyFile}.
     * <p>
     * NOTE: This method requires that the {@link WireMockServer} is configured to use WireKock's static DSL.
     *
     * @param mappingBuilder a builder with method and URL already configured
     * @return the created {@link StubMapping}
     */
    public static StubMapping mock(final MappingBuilder mappingBuilder, final ResponseDefinitionBuilder jsonResponseBuilder) {
        return stubFor(mappingBuilder
            .willReturn(jsonResponseBuilder));
    }

    /**
     * Instance-scoped (preferred for parallel tests).
     */
    public static StubMapping mock(final WireMockServer server, final MappingBuilder mappingBuilder, final ResponseDefinitionBuilder jsonResponseBuilder) {
        return server.stubFor(mappingBuilder
            .willReturn(jsonResponseBuilder));
    }

    /**
     * JSON-focused WireMock utility methods.
     */
    public static class Json {
        public static final String JSON_CONTENT_TYPE = "application/json";

        /**
         * Stubs a JSON response using a preconfigured {@link MappingBuilder}.
         * Sets status, {@code Content-Type: application/json; charset=UTF-8}, and serves the body via {@code bodyFile}.
         *
         * @param mappingBuilder a builder with method and URL already configured
         * @param status         HTTP status code (e.g., 200, 400, 503)
         * @param bodyFile       file path under {@code __files/} to serve as response body
         * @return the created {@link StubMapping}
         */
        public static StubMapping mock(final MappingBuilder mappingBuilder, final int status, final String bodyFile) {
            return stubFor(mappingBuilder
                .willReturn(jsonResponse(status, bodyFile, (Map<String, String>) null)));
        }

        /**
         * Instance-scoped JSON (preferred for parallel tests).
         */
        public static StubMapping mock(final WireMockServer server, final String urlPattern, final String bodyFile, final int status) {
            return server.stubFor(get(urlMatching(urlPattern)).willReturn(
                jsonResponse(status, bodyFile, (Map<String, String>) null)
            ));
        }

        /**
         * Stubs a GET JSON response using a preconfigured {@link MappingBuilder}.
         * Sets status, {@code Content-Type: application/json; charset=UTF-8}, and serves the body via {@code bodyFile}.
         *
         * @param urlPattern a URL pattern
         * @param status     HTTP status code (e.g., 200, 400, 503)
         * @param bodyFile   file path under {@code __files/} to serve as response body
         * @return the created {@link StubMapping}
         */
        public static StubMapping mockGet(final UrlPattern urlPattern, final int status, final String bodyFile) {
            return stubFor(get(urlPattern)
                .willReturn(jsonResponse(status, bodyFile, (Map<String, String>) null)));
        }

        /**
         * Stubs a POST JSON response using a preconfigured {@link MappingBuilder}.
         * Sets status, {@code Content-Type: application/json; charset=UTF-8}, and serves the body via {@code bodyFile}.
         *
         * @param urlPattern a URL pattern
         * @param status     HTTP status code (e.g., 200, 400, 503)
         * @param bodyFile   file path under {@code __files/} to serve as response body
         * @return the created {@link StubMapping}
         */
        public static StubMapping mockPost(final UrlPattern urlPattern, final int status, final String bodyFile) {
            return stubFor(post(urlPattern)
                .willReturn(jsonResponse(status, bodyFile, (Map<String, String>) null)));
        }

        /**
         * Stubs a PUT JSON response using a preconfigured {@link MappingBuilder}.
         * Sets status, {@code Content-Type: application/json; charset=UTF-8}, and serves the body via {@code bodyFile}.
         *
         * @param urlPattern a URL pattern
         * @param status     HTTP status code (e.g., 200, 400, 503)
         * @param bodyFile   file path under {@code __files/} to serve as response body
         * @return the created {@link StubMapping}
         */
        public static StubMapping mockPut(final UrlPattern urlPattern, final int status, final String bodyFile) {
            return stubFor(put(urlPattern)
                .willReturn(jsonResponse(status, bodyFile, (Map<String, String>) null)));
        }

        /**
         * Stubs a PATCH JSON response using a preconfigured {@link MappingBuilder}.
         * Sets status, {@code Content-Type: application/json; charset=UTF-8}, and serves the body via {@code bodyFile}.
         *
         * @param urlPattern a URL pattern
         * @param status     HTTP status code (e.g., 200, 400, 503)
         * @param bodyFile   file path under {@code __files/} to serve as response body
         * @return the created {@link StubMapping}
         */
        public static StubMapping mockPatch(final UrlPattern urlPattern, final int status, final String bodyFile) {
            return stubFor(patch(urlPattern)
                .willReturn(jsonResponse(status, bodyFile, (Map<String, String>) null)));
        }

        /**
         * Advanced: build a JSON response with optional extra headers or customizer.
         *
         * @param status       HTTP status
         * @param bodyFile     file path under {@code __files/}
         * @param extraHeaders optional extra headers (may be {@code null})
         * @param customizer   optional response customizer (delay, transformers, etc.), may be {@code null}
         * @return a {@link ResponseDefinitionBuilder}
         */
        public static ResponseDefinitionBuilder jsonResponse(final int status,
                                                             final String bodyFile,
                                                             @Nullable final Map<String, String> extraHeaders,
                                                             @Nullable final Consumer<ResponseDefinitionBuilder> customizer) {
            final ResponseDefinitionBuilder builder = aResponse()
                .withStatus(status)
                .withHeader(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE)
                .withBodyFile(bodyFile);

            if (extraHeaders != null) {
                extraHeaders.forEach(builder::withHeader);
            }

            if (customizer != null) {
                customizer.accept(builder);
            }

            return builder;
        }

        /**
         * Simplified overload of {@link #jsonResponse(int, String, Map, Consumer)} without customizer.
         */
        public static ResponseDefinitionBuilder jsonResponse(final int status,
                                                             final String bodyFile,
                                                             @Nullable final Map<String, String> extraHeaders) {
            return jsonResponse(status, bodyFile, extraHeaders, null);
        }

        /**
         * Simplified overload of {@link #jsonResponse(int, String, Map, Consumer)} without extra headers.
         */
        public static ResponseDefinitionBuilder jsonResponse(final int status,
                                                             final String bodyFile,
                                                             @Nullable final Consumer<ResponseDefinitionBuilder> customizer) {
            return jsonResponse(status, bodyFile, null, customizer);
        }
    }
}
