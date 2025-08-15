package io.github.finoid.testify.spring.http;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;

import java.util.Map;

@UtilityClass
public class HttpUtils {
    /**
     * Creates a map of http headers.
     *
     * @param headers map of headers to be added
     * @return the http headers
     */
    public static HttpHeaders headersOf(final Map<String, String> headers) {
        final HttpHeaders httpHeaders = new HttpHeaders();

        headers.forEach(httpHeaders::add);

        return httpHeaders;
    }
}
