package io.github.finoid.testify.spring.http.wiremock;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import io.github.finoid.testify.spring.type.IntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@ContextConfiguration(classes = {JacksonAutoConfiguration.class, CodecsAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
    RestClientAutoConfiguration.class, WireMockUtilsIntegrationTest.CustomTestConfiguration.class})
class WireMockUtilsIntegrationTest extends IntegrationTest {
    @RegisterExtension
    private static final WireMockExtension WIRE_MOCK = WireMockExtension.newInstance()
        .configureStaticDsl(true)
        .options(wireMockConfig()
            .dynamicPort()
            .dynamicHttpsPort()).build();

    @Autowired
    private RestClient defaultRestClient;

    @Test
    void givenMockedGetResponse_whenGetRequest_thenMockedResponseReturned() {
        WireMockUtils.Json.mockGet(urlMatching("/user.*"), 200, "example.rest.json");

        var responseEntity = defaultRestClient.get()
            .uri("/user/1")
            .retrieve()
            .toEntity(String.class);

        Assertions.assertEquals("{\"id\": 1, \"name\": \"Name\"}", responseEntity.getBody());
    }

    @SpringBootApplication
    private static class CustomApplication {
        @SuppressWarnings("required.method.not.called")
        public static void main(final String[] args) {
            SpringApplication.run(WireMockUtilsIntegrationTest.CustomApplication.class, args);
        }
    }

    @TestConfiguration
    static class CustomTestConfiguration {
        @Bean
        public RestClient defaultRestClient(final RestClient.Builder builder) {
            final WireMockRuntimeInfo runtimeInfo = WIRE_MOCK.getRuntimeInfo();

            return builder
                .baseUrl("http://localhost:" + runtimeInfo.getHttpPort())
                .build();
        }
    }
}