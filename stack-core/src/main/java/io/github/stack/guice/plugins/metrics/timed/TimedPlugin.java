package io.github.stack.guice.plugins.metrics.timed;

import com.google.inject.matcher.Matchers;
import io.github.stack.guice.plugins.StackPlugin;
import io.github.stack.guice.plugins.metrics.MetricsPlugin;

public class TimedPlugin extends StackPlugin {

    public TimedPlugin() {
        bindDependency(MetricsPlugin.class);
    }

    @Override
    protected void configure() {
        final TimedInterceptor interceptor = new TimedInterceptor();
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Timed.class), interceptor);
        requestInjection(interceptor);
    }
}
