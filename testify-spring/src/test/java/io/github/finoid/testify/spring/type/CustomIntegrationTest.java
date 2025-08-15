package io.github.finoid.testify.spring.type;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = CustomIntegrationTest.CustomTestConfiguration.class) // To prevent the whole application context from loading
class CustomIntegrationTest extends IntegrationTest {
    @Autowired
    private String hello;

    @Test
    void givenPartOfContext_whenLoadingApplicationContext_thenExpectedBeanLoaded() {
        Assertions.assertEquals("Hello World!", hello);
    }

    @TestConfiguration
    static class CustomTestConfiguration {
        @Bean
        public String hello() {
            return "Hello World!";
        }
    }
}
