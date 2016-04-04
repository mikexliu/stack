package io.github.mikexliu.stack.guice.plugins.stack.metrics;

import com.google.inject.Injector;
import com.google.inject.Scopes;
import io.github.mikexliu.stack.guice.plugins.stack.StackPlugin;

public class MetricsPlugin extends StackPlugin {

    public MetricsPlugin(final Injector injector) {
        super(injector);
    }

    @Override
    protected void configure() {
        bind(MetricsManagerResource.class).in(Scopes.SINGLETON);
    }
}
