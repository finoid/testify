package io.github.finoid.testify.spring.http.servlet;

import jakarta.validation.ValidationException;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import wiremock.org.apache.hc.core5.annotation.Internal;

/**
 * A custom {@link LocalValidatorFactoryBean} that integrates with the Spring {@link ApplicationContext}.
 * <p>
 * This class extends {@link LocalValidatorFactoryBean} and implements {@link ApplicationContextAware}
 * to ensure that Spring-managed beans can be injected into validation constraints.
 * It sets a custom {@link ConstraintValidatorFactory} to enable dependency injection into
 * {@link jakarta.validation.ConstraintValidator} instances.
 */
@Internal
public class ValidatorFactoryBean extends LocalValidatorFactoryBean implements ApplicationContextAware {
    @Override
    public void afterPropertiesSet() {
        // no-op
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        super.setApplicationContext(applicationContext);

        this.setConstraintValidatorFactory(new ConstraintValidatorFactory(applicationContext.getAutowireCapableBeanFactory()));

        try {
            super.afterPropertiesSet();
        } catch (final ValidationException ex) {
            LogFactory.getLog(getClass()).debug("Failed to set up a Bean Validation provider", ex);
        }
    }
}

