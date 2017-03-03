package io.github.stack.guice.plugins.metrics;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Scopes;
import io.github.stack.guice.plugins.StackPlugin;

public class MetricsPlugin extends StackPlugin {

    @Override
    protected void configure() {
        bind(MetricsManagerResource.class).in(Scopes.SINGLETON);

        bind(MetricRegistry.class).in(Scopes.SINGLETON);
        bind(MetricsManager.class).in(Scopes.SINGLETON);
    }
}
