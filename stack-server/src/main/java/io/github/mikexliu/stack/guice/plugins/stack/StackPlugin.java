package io.github.mikexliu.stack.guice.plugins.stack;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

public abstract class StackPlugin extends AbstractModule {

    protected final Injector injector;

    public StackPlugin(final Injector injector) {
        this.injector = injector;
    }
}
