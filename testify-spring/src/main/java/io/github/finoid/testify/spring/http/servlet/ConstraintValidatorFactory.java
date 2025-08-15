package io.github.finoid.testify.spring.http.servlet;

import jakarta.validation.ConstraintValidator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.util.Assert;

/**
 * A custom {@link jakarta.validation.ConstraintValidatorFactory} implementation
 * that integrates with Spring's dependency injection system.
 * <p>
 * This factory allows {@link ConstraintValidator} instances to be retrieved
 * from the Spring {@link AutowireCapableBeanFactory}, enabling them to be
 * autowired with Spring-managed beans.
 */
public class ConstraintValidatorFactory implements jakarta.validation.ConstraintValidatorFactory {
    private final AutowireCapableBeanFactory beanFactory;

    /**
     * Constructs a {@link ConstraintValidatorFactory} using the given Spring {@link AutowireCapableBeanFactory}.
     *
     * @param beanFactory the {@link AutowireCapableBeanFactory} used to manage constraint validators
     * @throws IllegalArgumentException if {@code beanFactory} is {@code null}
     */
    public ConstraintValidatorFactory(final AutowireCapableBeanFactory beanFactory) {
        Assert.notNull(beanFactory, "BeanFactory must not be null");
        this.beanFactory = beanFactory;
    }

    /**
     * Retrieves an instance of the requested {@link ConstraintValidator} class.
     * If an existing bean of the requested type is available in the Spring context, it is returned.
     * Otherwise, a new instance of the class is created and autowired by Spring.
     *
     * @param <T> the type of the {@link ConstraintValidator}
     * @param key the class of the validator to retrieve
     * @return an instance of the requested {@link ConstraintValidator}
     */
    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(final Class<T> key) {
        try {
            return this.beanFactory.getBean(key);
        } catch (final BeansException e) {
            // Fallback to creating a new instance if the bean is not found in the context
        }

        return this.beanFactory.createBean(key);
    }

    @Override
    public void releaseInstance(final ConstraintValidator<?, ?> instance) {
        this.beanFactory.destroyBean(instance);
    }
}
