package io.github.mikexliu.stack.guice.plugins.app;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import io.github.mikexliu.stack.guice.plugins.stack.StackPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AppPlugin extends AbstractModule {

    private Map<Class<?>, Class<? extends StackPlugin>> stackPluginDependencies = new HashMap<>();
    private Map<Class<?>, Class<? extends AppPlugin>> appPluginDependencies = new HashMap<>();

    /**
     * @param dependency
     * @param fromPlugin
     */
    public void bindDependency(final Class<?> dependency, final Class<?> fromPlugin) {
        if (StackPlugin.class.isAssignableFrom(fromPlugin)) {
            stackPluginDependencies.put(dependency, (Class) fromPlugin);
        } else if (AppPlugin.class.isAssignableFrom(fromPlugin)) {
            appPluginDependencies.put(dependency, (Class) fromPlugin);
        }
    }

    /**
     * @return
     */
    public final Set<Class<? extends StackPlugin>> getStackPluginDependencies() {
        return ImmutableSet.copyOf(stackPluginDependencies.values());
    }

    /**
     * @return
     */
    public final Set<Class<? extends AppPlugin>> getAppPluginDependencies() {
        return ImmutableSet.copyOf(appPluginDependencies.values());
    }
}
