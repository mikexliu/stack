package io.github.mikexliu.stack.guice.plugins.app;

import com.google.inject.AbstractModule;
import io.github.mikexliu.stack.guice.plugins.stack.StackPlugin;

import java.util.HashSet;
import java.util.Set;

public abstract class AppPlugin extends AbstractModule {

    /**
     *
     * @return
     */
    public Set<Class<? extends AppPlugin>> getAppPluginDependencies() {
        return new HashSet<>();
    }

    /**
     *
     * @return
     */
    public Set<Class<? extends StackPlugin>> getStackPluginDependencies() {
        return new HashSet<>();
    }
}
