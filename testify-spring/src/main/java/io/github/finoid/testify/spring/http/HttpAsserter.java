package io.github.finoid.testify.spring.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finoid.testify.core.internal.Precondition;
import io.github.finoid.testify.spring.http.HttpAsserter.ExpectSpec.ExpectDsl;
import io.github.finoid.testify.spring.http.servlet.MockMvcBuilder;
import io.github.finoid.testify.spring.http.servlet.ValidatorFactoryBean;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class for making HTTP requests and asserting responses in tests.
 * <p>
 * This class provides convenient methods to perform HTTP requests (GET, POST, PUT, PATCH, DELETE)
 * using {@link MockMvc} and validate the responses against expected HTTP status codes.
 * It supports JSON serialization and deserialization using {@link ObjectMapper}.
 * Example usage:
 * <pre>{@code
 * var requestSpec = HttpAsserter.RequestSpec.post("/api/users")
 *      .withBody(new CreateUserRequest("John"))
 *      .andExpect()
 *      .status(HttpStatus.CREATED)
 *      .responseOf(new TypeReference<UserResponse>() {});
 *
 * HttpAsserter asserter = new HttpAsserter(mockMvc, objectMapper);
 * var response = asserter.perform(requestSpec);
 * }</pre>
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpAsserter {
    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    /**
     * Factory method to create a {@link HttpAsserter} instance using the given {@link MockMvc} and {@link ObjectMapper}.
     *
     * @param mockMvc      the mock mvc instance
     * @param objectMapper the object mapper instance
     * @return a new {@link HttpAsserter}
     */
    public static HttpAsserter ofMockMvcAndObjectMapper(final MockMvc mockMvc, final ObjectMapper objectMapper) {
        return new HttpAsserter(
            Precondition.nonNull(mockMvc, "MockMvc must not be null."),
            Precondition.nonNull(objectMapper, "ObjectMapper must not be null.")
        );
    }

    /**
     * Performs an HTTP request using the given {@link HttpRequestSpec} and returns the response body.
     *
     * @param httpRequestSpec the HTTP request and expected response
     * @param <T>             the expected response type
     * @return the response body, either as a deserialized object or raw string
     */
    public <T> RequestBody<T> perform(final HttpRequestSpec<T> httpRequestSpec) {
        return perform(httpRequestSpec, response -> {
        });
    }

    /**
     * Performs an HTTP request using the given {@link HttpRequestSpec} and applies the provided custom assertion.
     *
     * @param httpRequestSpec        the HTTP request and expected response
     * @param assertResponseFunction additional assertions on the raw response
     * @param <T>                    the expected response type
     * @return the response body, either as a deserialized object or raw string
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> RequestBody<T> perform(final HttpRequestSpec<T> httpRequestSpec, final AssertResponseFunction assertResponseFunction) {
        String responseBodyString = null;

        try {
            final MockHttpServletResponse response = request(httpRequestSpec.requestSpec)
                .andReturn()
                .getResponse();

            final String content = response.getContentAsString();

            final HttpStatus expectedHttpStatus = httpRequestSpec.expectSpec.status;

            Assertions.assertEquals(expectedHttpStatus.value(), response.getStatus(),
                () -> String.format("Not equal. %d - %d Response: %s ", expectedHttpStatus.value(), response.getStatus(), content));

            assertResponseFunction.assertFunc(response);

            responseBodyString = content;

            if (httpRequestSpec.optionalResponseTypeReference().isPresent()) {
                return new RequestBody.TypeBody<>(objectMapper.readValue(responseBodyString, httpRequestSpec.optionalResponseTypeReference().orElseThrow()));
            }

            return new RequestBody.StringBody(responseBodyString);
        } catch (final Exception e) {
            return Assertions.fail("Error during request. Cause: " + e.getMessage(), e);
        }
    }

    private ResultActions request(final RequestSpec requestSpec) throws Exception {
        final MockHttpServletRequestBuilder builder = requestSpec.getHttpMethod()
            .getBuilderResolver()
            .resolve(requestSpec.url)
            .params(requestSpec.parameters);

        return mvc.perform(builder
            .accept(requestSpec.getAcceptTypes().toArray(MediaType[]::new))
            .contentType(requestSpec.getContentType())
            .headers(requestSpec.httpHeaders)
            .content(writeValueAsString(requestSpec.getBody()))
        );
    }

    @SneakyThrows(JsonProcessingException.class)
    private <I> String writeValueAsString(@org.jspecify.annotations.Nullable final I request) {
        if (request == null) {
            return "";
        }

        if (request instanceof final String string) {
            return string;
        }

        return this.objectMapper.writeValueAsString(request);
    }

    /**
     * DSL for building up an HTTP asserter.
     */
    public static class HttpAsserterDsl {
        private ObjectMapper objectMapper = new ObjectMapper();
        @Nullable
        private Object controller;
        private Collection<Object> controllerAdvices = Collections.emptyList();
        private FormattingConversionService conversionService = new DefaultFormattingConversionService();
        private Collection<HandlerExceptionResolver> handlerExceptionResolvers = Collections.emptyList();
        private Validator validator = new ValidatorFactoryBean();
        private Collection<Object> additionalBeans = Collections.emptyList();
        private Collection<HttpMessageConverter<?>> httpMessageConverters = Collections.emptyList();

        private HttpAsserterDsl() {
        }

        private HttpAsserterDsl(final Object controller) {
            this.controller = controller;
        }

        /**
         * Starts building a new {@link HttpAsserterDsl} for the given controller.
         */
        public static HttpAsserterDsl ofController(final Object controller) {
            return new HttpAsserterDsl(controller);
        }

        /**
         * Starts building a new {@link HttpAsserterDsl} with default configuration.
         */
        public static HttpAsserterDsl defaultConfiguration() {
            return new HttpAsserterDsl();
        }

        public HttpAsserterDsl objectMapper(final ObjectMapper objectMapper) {
            this.objectMapper = Precondition.nonNull(objectMapper, "Object mapper must not be null");

            return this;
        }

        public HttpAsserterDsl controller(final Object controller) {
            this.controller = Precondition.nonNull(controller, "Controller must not be null");

            return this;
        }

        public HttpAsserterDsl controllerAdvices(final Collection<Object> controllerAdvices) {
            this.controllerAdvices = List.copyOf(Precondition.nonNull(controllerAdvices, "Controller advices must not be null"));

            return this;
        }

        public HttpAsserterDsl conversionService(final FormattingConversionService conversionService) {
            this.conversionService = Precondition.nonNull(conversionService, "Conversion service must not be null");

            return this;
        }

        public HttpAsserterDsl handlerExceptionResolvers(final Collection<HandlerExceptionResolver> handlerExceptionResolvers) {
            this.handlerExceptionResolvers = handlerExceptionResolvers;

            return this;
        }

        public HttpAsserterDsl handlerExceptionResolvers(final HandlerExceptionResolver... handlerExceptionResolvers) {
            this.handlerExceptionResolvers = List.of(handlerExceptionResolvers);

            return this;
        }

        public HttpAsserterDsl validator(final Validator validator) {
            this.validator = Precondition.nonNull(validator, "Validator must not be null");

            return this;
        }

        public HttpAsserterDsl additionalBeans(final Collection<Object> additionalBeans) {
            this.additionalBeans = List.copyOf(Precondition.nonNull(additionalBeans, "Additional beans must not be null"));

            return this;
        }

        public HttpAsserterDsl httpMessageConverters(final Collection<HttpMessageConverter<?>> httpMessageConverters) {
            this.httpMessageConverters = List.copyOf(Precondition.nonNull(httpMessageConverters, "HTTP message converters must not be null"));

            return this;
        }

        /**
         * Builds the configured {@link HttpAsserter}.
         */
        public HttpAsserter toHttpAsserter() {
            final MockMvcBuilder builder = (controller != null ? new MockMvcBuilder(controller) : new MockMvcBuilder())
                .setMessageConverters(httpMessageConverters.toArray(HttpMessageConverter[]::new))
                .setHandlerExceptionResolvers(handlerExceptionResolvers.toArray(HandlerExceptionResolver[]::new))
                .setControllerAdvice(controllerAdvices.toArray(Object[]::new))
                .setConversionService(conversionService)
                .setValidator(validator)
                .setAdditionalBeans(List.copyOf(additionalBeans));

            return new HttpAsserter(builder.build(), objectMapper);
        }
    }

    /**
     * Functional interface for custom assertions on the raw HTTP response.
     */
    @FunctionalInterface
    public interface AssertResponseFunction {
        /**
         * Applies custom assertions to the response.
         *
         * @param response the raw {@link MockHttpServletResponse}
         */
        void assertFunc(final MockHttpServletResponse response);
    }

    /**
     * Represents a response body returned from an HTTP request, either as a raw string or deserialized object.
     */
    @SuppressWarnings("checkstyle:InnerTypeLast")
    public sealed interface RequestBody<T> {
        /**
         * Wrapper for a deserialized body of type T.
         */
        record TypeBody<T>(T body) implements RequestBody<T> {
        }

        /**
         * Wrapper for a raw string response body.
         */
        record StringBody<T extends String>(T body) implements RequestBody<T> {
        }

        /**
         * Returns the deserialized body if this is a {@link TypeBody}, or {@code null} otherwise.
         */
        @Nullable
        default T deserializedOrNull() {
            //noinspection SwitchStatementWithTooFewBranches
            return switch (this) {
                case TypeBody<T>(T body) -> body;
                default -> null;
            };
        }

        /**
         * Returns the deserialized body if this is a {@link TypeBody}, or throws {@link IllegalArgumentException} otherwise.
         *
         * @throws IllegalArgumentException if the response body is not a deserialized object
         */
        default T deserializedOrThrow() {
            //noinspection SwitchStatementWithTooFewBranches
            return switch (this) {
                case TypeBody<T>(T body) -> body;
                default -> throw new IllegalArgumentException("Response body is not a deserialized object.");
            };
        }

        /**
         * Returns the raw string body if this is a {@link StringBody}, or {@code null} otherwise.
         */
        @Nullable
        @SuppressWarnings("rawtypes")
        default String stringOrNull() {
            //noinspection SwitchStatementWithTooFewBranches
            return switch (this) {
                case StringBody sb -> sb.body;
                default -> null;
            };
        }

        /**
         * Returns the raw string body if this is a {@link StringBody}, or throws {@link IllegalArgumentException} otherwise.
         *
         * @throws IllegalArgumentException if the response body is not a raw string
         */
        @Nullable
        @SuppressWarnings("rawtypes")
        default String stringOrThrow() {
            //noinspection SwitchStatementWithTooFewBranches
            return switch (this) {
                case StringBody sb -> sb.body;
                default -> throw new IllegalArgumentException("Response body is not a raw string.");
            };
        }

        /**
         * Returns the deserialized body if this is a {@link TypeBody}, otherwise {@link Optional#empty()}.
         *
         * @return Optional of deserialized body
         */
        default Optional<T> deserialized() {
            return Optional.ofNullable(deserializedOrNull());
        }

        /**
         * Returns the raw string body if this is a {@link StringBody}, otherwise {@link Optional#empty()}.
         *
         * @return Optional of raw string body
         */
        default Optional<String> rawString() {
            return Optional.ofNullable(stringOrNull());
        }
    }

    /**
     * Describes the expected HTTP response, including status code and optional body type.
     *
     * @param <R> the expected response type
     */
    @Value
    public static class ExpectSpec<R> {
        HttpStatus status;
        @Nullable
        TypeReference<R> responseTypeReference;
        boolean isResponseAsString;

        /**
         * Create an expectation spec for a typed response body.
         */
        public static <R> ExpectSpec<R> asResponseType(final HttpStatus status, @Nullable final TypeReference<R> responseTypeReference) {
            return new ExpectSpec<>(status, responseTypeReference, false);
        }

        /**
         * Create an expectation spec where the response is a raw string.
         */
        public static <R> ExpectSpec<R> asResponseString(final HttpStatus status) {
            return new ExpectSpec<>(status, null, true);
        }

        public Optional<TypeReference<R>> optionalResponseTypeReference() {
            return Optional.ofNullable(responseTypeReference);
        }

        /**
         * DSL for defining expected response behavior.
         */
        @SuppressWarnings("NullAway.Init")
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class ExpectDsl {
            private final RequestSpec requestSpec;

            private HttpStatus status = HttpStatus.OK;

            public static ExpectDsl ofSpec(final RequestSpec requestSpec) {
                return new ExpectDsl(requestSpec);
            }

            /**
             * Defines the expected HTTP status code.
             *
             * @param expectedStatus the expected status
             * @return the current DSL instance
             */
            public ExpectDsl status(final HttpStatus expectedStatus) {
                this.status = Precondition.nonNull(expectedStatus, "Expected status must not be null.");

                return this;
            }

            /**
             * Defines a typed response body to deserialize.
             *
             * @param typeReference the expected response type
             * @param <R>           the response type
             * @return the combined HTTP request and response spec
             */
            public <R> HttpRequestSpec<R> responseOf(final TypeReference<R> typeReference) {
                return HttpRequestSpec.ofSpecs(requestSpec, ExpectSpec.asResponseType(status, typeReference));

            }

            /**
             * Defines a response expected to be treated as a raw string.
             *
             * @return the combined HTTP request and response spec
             */
            public HttpRequestSpec<?> responseOfString() {
                return HttpRequestSpec.ofSpecs(requestSpec, ExpectSpec.asResponseString(status));
            }
        }
    }

    /**
     * Combines the request specification and expected response for a single HTTP test case.
     *
     * @param <R> the expected response type
     */
    @Value
    public static class HttpRequestSpec<R> {
        RequestSpec requestSpec;
        ExpectSpec<R> expectSpec;

        public static <R> HttpRequestSpec<R> ofSpecs(final RequestSpec requestSpec, final ExpectSpec<R> expectSpec) {
            return new HttpRequestSpec<>(requestSpec, expectSpec);
        }

        public Optional<TypeReference<R>> optionalResponseTypeReference() {
            return expectSpec.optionalResponseTypeReference();
        }

        /**
         * DSL for building up an HTTP request with expectation.
         */
        @RequiredArgsConstructor
        public static class HttpRequestDsl {
            private final RequestSpec spec;

            public static HttpRequestDsl ofSpec(final RequestSpec spec) {
                return new HttpRequestDsl(spec);
            }

            public ExpectDsl andExpect() {
                return new ExpectDsl(spec);
            }
        }
    }

    /**
     * Specification for constructing an HTTP request.
     * Includes HTTP method, URL, headers, body, and accepted content types.
     */
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RequestSpec {
        HttpMethod httpMethod;
        String url;
        @SuppressWarnings("deprecation")
        Set<MediaType> acceptTypes;
        MediaType contentType;
        HttpHeaders httpHeaders;
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        @Nullable
        Object body;

        public static RequestSpecDsl get(final String url) {
            return RequestSpecDsl.spec(url)
                .withHttpMethod(HttpMethod.GET);
        }

        public static RequestSpecDsl head(final String url) {
            return RequestSpecDsl.spec(url)
                .withHttpMethod(HttpMethod.HEAD);
        }

        public static RequestSpecDsl post(final String url) {
            return RequestSpecDsl.spec(url)
                .withHttpMethod(HttpMethod.POST);
        }

        public static RequestSpecDsl put(final String url) {
            return RequestSpecDsl.spec(url)
                .withHttpMethod(HttpMethod.PUT);
        }

        public static RequestSpecDsl patch(final String url) {
            return RequestSpecDsl.spec(url)
                .withHttpMethod(HttpMethod.PATCH);
        }

        public static RequestSpecDsl delete(final String url) {
            return RequestSpecDsl.spec(url)
                .withHttpMethod(HttpMethod.DELETE);
        }

        public static RequestSpecDsl options(final String url) {
            return RequestSpecDsl.spec(url)
                .withHttpMethod(HttpMethod.OPTIONS);
        }

        public static RequestSpecDsl ofUrlAndHttpMethod(final String url, final HttpMethod httpMethod) {
            return RequestSpecDsl.spec(url)
                .withHttpMethod(httpMethod);
        }

        /**
         * DSL for incrementally constructing a {@link RequestSpec}.
         */
        public static class RequestSpecDsl {
            String url;
            HttpMethod httpMethod = HttpMethod.GET;
            @SuppressWarnings("deprecation")
            Set<MediaType> acceptTypes = Set.of(MediaType.APPLICATION_JSON_UTF8);
            MediaType contentType = MediaType.APPLICATION_JSON;
            HttpHeaders httpHeaders = new HttpHeaders();
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            @Nullable
            Object body;

            private RequestSpecDsl(final String url) {
                this.url = Precondition.nonNull(url, "Url must not be null.");
            }

            public static RequestSpecDsl spec(final String url) {
                return new RequestSpecDsl(url);
            }

            public RequestSpecDsl withUrl(final String url) {
                this.url = Precondition.nonNull(url);

                return this;
            }

            public RequestSpecDsl withHttpMethod(final HttpMethod httpMethod) {
                this.httpMethod = Precondition.nonNull(httpMethod, "HttpMethod must not be null.");

                return this;
            }

            public RequestSpecDsl withAcceptTypes(final Collection<MediaType> acceptTypes) {
                this.acceptTypes = Set.copyOf(Precondition.nonNull(acceptTypes, "AcceptTypes must not be null."));

                return this;
            }

            public RequestSpecDsl withContentType(final MediaType contentType) {
                this.contentType = Precondition.nonNull(contentType, "ContentType must not be null.");

                return this;
            }

            public RequestSpecDsl withHttpHeaders(final HttpHeaders httpHeaders) {
                this.httpHeaders = Precondition.nonNull(httpHeaders, "HttpHeaders must not be null.");

                return this;
            }

            public RequestSpecDsl withParam(String name, String... values) {
                addToMultiValueMap(this.parameters, name, values);

                return this;
            }

            public RequestSpecDsl withBody(@Nullable final Object body) {
                this.body = body;

                return this;
            }

            /**
             * Begins defining the expected response for the built request.
             *
             * @return DSL to specify status and response body type
             */
            public ExpectDsl andExpect() {
                return ExpectDsl.ofSpec(toSpec());
            }

            /**
             * Builds the {@link RequestSpec} from configured fields.
             *
             * @return the request specification
             */
            public RequestSpec toSpec() {
                return new RequestSpec(httpMethod, url, acceptTypes, contentType, httpHeaders, parameters, body);
            }

            private static <T> void addToMultiValueMap(final MultiValueMap<String, T> map, final String name, final T[] values) {
                Assert.hasLength(name, "'name' must not be empty");
                Assert.notEmpty(values, "'values' must not be empty");
                for (T value : values) {
                    map.add(name, value);
                }
            }
        }

        /**
         * Enum representing supported HTTP methods and their corresponding {@link MockMvcRequestBuilders}.
         */
        @Getter
        @RequiredArgsConstructor
        public enum HttpMethod {
            GET(MockMvcRequestBuilders::get),
            HEAD(MockMvcRequestBuilders::head),
            POST(MockMvcRequestBuilders::post),
            PUT(MockMvcRequestBuilders::put),
            PATCH(MockMvcRequestBuilders::patch),
            DELETE(MockMvcRequestBuilders::delete),
            OPTIONS(MockMvcRequestBuilders::options);

            @SuppressWarnings("ImmutableEnumChecker")
            private final HttpMethodBuilderResolver builderResolver;
        }
    }

    /**
     * Functional interface for resolving an HTTP method into a {@link MockHttpServletRequestBuilder}.
     */
    @FunctionalInterface
    interface HttpMethodBuilderResolver {
        /**
         * Resolves the given URI into a {@link MockHttpServletRequestBuilder} for the corresponding HTTP method.
         *
         * @param uri the target URI
         * @return the request builder for the HTTP method
         */
        MockHttpServletRequestBuilder resolve(final String uri);
    }
}
