package io.github.finoid.testify.spring.autoconfigure;

import io.github.finoid.testify.spring.http.HttpAsserter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class TestifyAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(TestifyAutoConfiguration.class))
        .withUserConfiguration(CustomTestConfiguration.class);

    @Test
    void givenBeanIsMissing_whenContextRun_thenExpectedBeanIsCreated() {
        contextRunner
            .run((context) -> assertThat(context)
                .hasSingleBean(HttpAsserter.class));
    }

    @TestConfiguration
    public static class CustomTestConfiguration {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public MockMvc mockMvc() {
            return Mockito.mock(MockMvc.class);
        }
    }
}