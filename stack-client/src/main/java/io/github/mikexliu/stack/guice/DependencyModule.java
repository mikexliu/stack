package io.github.mikexliu.stack.guice;

import com.google.inject.AbstractModule;

import java.util.Arrays;
import java.util.Collection;

/**
 * Add the Resources
 */
public class DependencyModule extends AbstractModule {

    private final Collection<Class<?>> resourceClasses;

    /**
     * TODO: use a builder for this
     * @param resourceClasses
     */
    public DependencyModule(final Class<?>... resourceClasses) {
        this.resourceClasses = Arrays.asList(resourceClasses);
    }

    @Override
    protected void configure() {
        for (final Class<?> resourceClass : resourceClasses) {
            // bind resources
        }
    }
}
