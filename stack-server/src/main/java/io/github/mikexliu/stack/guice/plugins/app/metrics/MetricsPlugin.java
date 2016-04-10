package io.github.mikexliu.stack.guice.plugins.app.metrics;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Scopes;
import io.github.mikexliu.stack.guice.plugins.app.AppPlugin;

public class MetricsPlugin extends AppPlugin {

    @Override
    protected void configure() {
        bind(MetricsManagerResource.class).in(Scopes.SINGLETON);

        bind(MetricRegistry.class).in(Scopes.SINGLETON);
        bind(MetricsManager.class).in(Scopes.SINGLETON);
    }
}
