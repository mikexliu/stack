package io.github.stack.guice.plugins;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;

import java.util.HashSet;
import java.util.Set;

public abstract class StackPlugin extends AbstractModule {

    private Set<Class<? extends StackPlugin>> dependencies = new HashSet<>();

    /**
     * @param dependencyPlugin
     */
    public void bindDependency(final Class<? extends StackPlugin> dependencyPlugin) {
            dependencies.add(dependencyPlugin);
    }

    /**
     * @return
     */
    public final Set<Class<? extends StackPlugin>> getDependencies() {
        return ImmutableSet.copyOf(dependencies);
    }
}
