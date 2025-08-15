package io.github.finoid.testify.spring.http;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * A no-operation (no-op) implementation of the {@link Validator} interface.
 * This can be used as a placeholder when a validator is required but no actual validation is needed.
 */
public class NoOpValidator implements Validator {
    @Override
    public boolean supports(final Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        // no-op
    }
}
