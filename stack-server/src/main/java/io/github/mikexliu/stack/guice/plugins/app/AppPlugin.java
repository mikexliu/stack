package io.github.mikexliu.stack.guice.plugins.app;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;

import java.util.HashSet;
import java.util.Set;

public abstract class AppPlugin extends AbstractModule {

//    private Map<Class<?>, Class<? extends StackPlugin>> stackPluginDependencies = new HashMap<>();
    private Set<Class<? extends AppPlugin>> appPluginDependencies = new HashSet<>();

    /**
     * @param dependencyPlugin
     */
    public void bindDependency(final Class<? extends AppPlugin> dependencyPlugin) {
            appPluginDependencies.add(dependencyPlugin);
    }

    /**
     * @return
     */
    public final Set<Class<? extends AppPlugin>> getAppPluginDependencies() {
        return ImmutableSet.copyOf(appPluginDependencies);
    }
}
