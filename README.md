# testify

A Java library designed to streamline testing by integrating Testcontainers, data fakers, test fixtures, snapshot
testing, and more - enabling efficient test automation.

<div align="center">
  <img src=".github/assets/finoid-testify.png" width="256" alt="Testify Library Logo"/>
  <p style="font-size: 10px">
  </p>
</div>

## Modules

* **testify-core** - Core module providing data fakers, and JSON serialization utilities.
* **testify-snapshot** - Snapshot module providing snapshot testing utilities
* **testify-spring** - Spring module for end-to-end (E2E), integration, and API testing.
* **testify-spring-autoconfigure** - Spring Auto-Configuration module for automatically bootstrapping and configuring
  beans from other Testify modules.
* **testify-testcontainers** - Testcontainers support, including JUnit extensions for MsSqlServer.

## Examples

### [Snapshot tests](testify-snapshot/src/test/java/io/github/finoid/testify/snapshot/SnapshotterExtensionTest.java)

Snapshot testing is ideal for verifying the result of a method or API call. It eliminates the need for complex
assertions by capturing and validating the entire output.

* Improves readability - Tests focus on intent instead of large blocks of assertion code.
* Encourages consistency - Helps keep test coverage broad without relying on selective, fragile assertions.
* Simple maintenance - When intentional changes occur, updating the snapshot is straightforward.
* Great for complex domains - Especially useful when objects are deeply nested, JSON-heavy, or include many fields.
* Safer refactoring - Changes in output are caught automatically, so unintended side effects are detected.

```java

@Test
void givenValidUserRequest_whenRequestingUser_thenExpectedUserIsReturned(Snapshotter snapshotter) {
    // Request setup omitted for brevity in this example
    var response = new UserResponse(1, "John");

    snapshotter.json()
        .snapshot(new User(1, "John"));
}
```

### [Spring API tests](testify-spring/src/test/java/io/github/finoid/testify/spring/type/CustomApiTest.java)

API tests ensure the API contract by validating requests, responses, and input handling without starting the entire
application context. This results in fast execution and makes it easy to test advice controllers.

* Early feedback - Fail fast during development without requiring a full application or database.
* Speed - Run significantly faster than full-blown integration or end-to-end tests.
* Isolation - Focus on the API layer without depending on UI, persistence, or other external systems.
* Test edge cases - Easy to test edge cases and error scenarios (validation errors, exceptions, advice controllers).
* Confidence in refactoring - Ensures that underlying changes don’t break the API contract.

```java
class CustomApiTest extends ApiTest {
    @Autowired
    private HttpAsserter.HttpAsserterDsl dsl;

    @Test
    void givenHttpRequest_whenRequestAndJsonResponse_thenSuccessfulJsonResponse() {
        var asserter = dsl.controller(new ExampleController())
            .toHttpAsserter();

        var httpRequestSpec = RequestSpec.get("/v1/hello")
            .andExpect()
            .status(HttpStatus.OK)
            .responseOf(new TypeReference<User>() {
            });

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

    record User(int id, String name) {
    }
}
```

### [Spring e2e api tests](testify-spring/src/test/java/io/github/finoid/testify/spring/type/CustomEndToEndHttpTest.java)

End-to-end API tests ensure not only that the API behaves correctly, but also that underlying components such as
persistence and domain logic function as expected.

* Realistic execution - Exercise the system as a whole, in conditions close to production.
* Integration assurance - Validate that different layers (API, services, persistence, external systems) work correctly
  together.
* Catch hidden issues - Reveal configuration problems, wiring errors, or mismatches that unit or API-only tests might
  miss.
* User journey validation - Ensure that real-world workflows (e.g., creating a user, making a transaction) succeed from
  start to finish.

```java

@Import(CustomEndToEndHttpTest.ExampleController.class)
class CustomEndToEndHttpTest extends EndToEndHttpTest {
    @Autowired
    private HttpAsserter asserter;

    @Test
    void givenHttpRequest_whenRequestAndJsonResponse_thenSuccessfulJsonResponse() {
        var httpRequestSpec = RequestSpec.get("/v1/hello")
            .andExpect()
            .status(HttpStatus.OK)
            .responseOf(new TypeReference<User>() {
            });

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

    record User(int id, String name) {
    }

    @SpringBootApplication
    private static class CustomApplication {
        @SuppressWarnings("required.method.not.called")
        public static void main(final String[] args) {
            SpringApplication.run(CustomApplication.class, args);
        }
    }
}
```

### Test base classes

| Name                 | Description                                                                                                                                                                 | @Tag                       |
|----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------|
| **UnitTest**         | Verifies a single class or method in isolation. Runs quickly and can be executed in parallel.                                                                               | `@Tag("UnitTest")`         |
| **IntegrationTest**  | Validates the interaction of multiple components. Loads only the required parts of the Spring context using `@ContextConfiguration`.                                        | `@Tag("IntegrationTest")`  |
| **ApiTest**          | Ensures the API contract by validating requests, responses, and input handling without starting the full application context.                                               | `@Tag("ApiTest")`          |
| **EndToEndHttpTest** | Exercises the API together with underlying persistence and domain logic to verify end-to-end workflows. External rest-integrations can be mocked to keep the scope focused. | `@Tag("EndToEndHttpTest")` |

#### Reasons behind base classes types?
Each base class type represents a specific level of testing (unit, integration, API, or end-to-end). The applied `@Tag`
annotations make it easy to filter and select which tests should run in a given test plan.

### Testcontainers

```java
class PostgresContainerExtensionTest {
    @RegisterExtension
    static final PostgresContainerExtension POSTGRES = PostgresContainerExtension.create();

    @Test
    void givenContainerExtension_whenTestRuns_thenContainerIsRunning() {
        Assertions.assertTrue(POSTGRES.isContainerRunning());
    }
}
```