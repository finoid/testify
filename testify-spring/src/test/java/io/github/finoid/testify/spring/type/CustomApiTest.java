package io.github.finoid.testify.spring.type;

import io.github.finoid.testify.spring.http.HttpAsserter;
import io.github.finoid.testify.spring.http.HttpAsserter.RequestSpec;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.MultiValueMap;
import tools.jackson.core.type.TypeReference;

class CustomApiTest extends ApiTest {
    @Autowired
    private HttpAsserter.HttpAsserterDsl dsl;

    @Test
    void givenHttpRequest_whenRequestAndPlainResponse_thenSuccessfulPlainResponse() {
        var asserter = dsl.controller(new ExampleController())
            .toHttpAsserter();

        var httpRequestSpec = RequestSpec.get("/v1/helloPlain")
            .andExpect()
            .status(HttpStatus.OK)
            .responseOfString();

        var result = asserter.perform(httpRequestSpec);

        Assertions.assertEquals("Hello World!", result.stringOrNull());
    }

    @Test
    void givenHttpRequest_whenRequestAndJsonResponse_thenSuccessfulJsonResponse() {
        var asserter = dsl.controller(new ExampleController())
            .toHttpAsserter();

        var httpRequestSpec = RequestSpec.get("/v1/helloJson")
            .andExpect()
            .status(HttpStatus.OK)
            .responseOf(new TypeReference<User>() {
            });

        var result = asserter.perform(httpRequestSpec);

        Assertions.assertEquals(new User(1, "hello"), result.deserializedOrNull());
    }

    @Test
    void givenQueryParamsAndRemoteAddress_whenRequest_thenBuilderAppliesThem() {
        var asserter = dsl.controller(new ExampleController())
            .toHttpAsserter();

        var httpRequestSpec = RequestSpec.get("/v1/requestInfo")
            .withQueryParam("tag", "alpha", "beta")
            .withRemoteAddress("203.0.113.9")
            .andExpect()
            .status(HttpStatus.OK)
            .responseOf(new TypeReference<RequestInfo>() {
            });

        var result = asserter.perform(httpRequestSpec);

        Assertions.assertEquals(new RequestInfo(java.util.List.of("alpha", "beta"), null, "203.0.113.9"), result.deserializedOrNull());
    }

    @Test
    void givenFormFields_whenPostRequest_thenBuilderAppliesThem() {
        var asserter = dsl.controller(new ExampleController())
            .toHttpAsserter();

        var httpRequestSpec = RequestSpec.post("/v1/requestInfo")
            .withContentType(MediaType.APPLICATION_FORM_URLENCODED)
            .withFormField("name", "Ada")
            .withFormField("role", "admin", "auditor")
            .andExpect()
            .status(HttpStatus.OK)
            .responseOf(new TypeReference<RequestInfo>() {
            });

        var result = asserter.perform(httpRequestSpec);

        Assertions.assertEquals(new RequestInfo(null, java.util.List.of("Ada", "admin", "auditor"), null), result.deserializedOrNull());
    }

    @RestController
    static class ExampleController {
        @RequestMapping("/v1/helloPlain")
        public String helloPlain() {
            return "Hello World!";
        }

        @RequestMapping("/v1/helloJson")
        public User helloJson() {
            return new User(1, "hello");
        }

        @GetMapping("/v1/requestInfo")
        public RequestInfo requestInfo(@RequestParam(name = "tag", required = false) final java.util.List<String> tags,
                                       final HttpServletRequest request) {
            return new RequestInfo(tags, null, request.getRemoteAddr());
        }

        @PostMapping(path = "/v1/requestInfo", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        public RequestInfo requestInfo(@RequestParam final MultiValueMap<String, String> formFields) {
            return new RequestInfo(null, formFields.values().stream().flatMap(java.util.Collection::stream).toList(), null);
        }
    }

    record User(int id, String name) {
    }

    record RequestInfo(java.util.List<String> queryTags, java.util.List<String> formValues, String remoteAddress) {
    }
}
