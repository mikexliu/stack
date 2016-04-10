package io.github.mikexliu.stack.guice.plugins.app.timed;

import com.google.inject.matcher.Matchers;
import io.github.mikexliu.stack.guice.plugins.app.AppPlugin;
import io.github.mikexliu.stack.guice.plugins.stack.metrics.MetricsManager;
import io.github.mikexliu.stack.guice.plugins.stack.metrics.MetricsPlugin;

public class TimedPlugin extends AppPlugin {

    public TimedPlugin() {
        bindDependency(MetricsManager.class, MetricsPlugin.class);
    }

    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Timed.class), new TimedInterceptor());
    }
}
