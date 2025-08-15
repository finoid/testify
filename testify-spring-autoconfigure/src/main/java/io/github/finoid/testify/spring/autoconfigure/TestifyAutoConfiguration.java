package io.github.finoid.testify.spring.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finoid.testify.spring.http.HttpAsserter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfiguration
public class TestifyAutoConfiguration {
    @Bean
    @ConditionalOnBean({ObjectMapper.class, MockMvc.class})
    @ConditionalOnMissingBean(HttpAsserter.class)
    public HttpAsserter httpAsserter(final ObjectMapper objectMapper, final MockMvc mockMvc) {
        return HttpAsserter.ofMockMvcAndObjectMapper(mockMvc, objectMapper);
    }
}