package io.github.finoid.testify.spring.type;

import io.github.finoid.testify.spring.http.HttpAsserter;
import io.github.finoid.testify.spring.http.HttpAsserter.RequestSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.type.TypeReference;

@Import(CustomEndToEndHttpTest.ExampleController.class)
class CustomEndToEndHttpTest extends EndToEndHttpTest {
    @Autowired
    private HttpAsserter asserter;

    @Test
    void givenHttpRequest_whenRequestAndJsonResponse_thenSuccessfulJsonResponse() {
        var httpRequestSpec = RequestSpec.get("/v1/hello")
            .andExpect()
            .status(HttpStatus.OK)
            .responseOf(new TypeReference<User>() {});

        var result = asserter.perform(httpRequestSpec);

        Assertions.assertEquals(new User(1, "hello"), result.deserializedOrNull());
    }

    @RestController
    static class ExampleController {
        @RequestMapping("/v1/hello")
        public User helloJson() {
            return new User(1, "hello");
        }
    }

    record User(int id, String name) {}

    @SpringBootApplication
    private static class CustomApplication {
        @SuppressWarnings("required.method.not.called")
        public static void main(final String[] args) {
            SpringApplication.run(CustomApplication.class, args);
        }
    }
}
