package io.github.mikexliu.stack.guice.plugins.app.timed;

import com.google.inject.matcher.Matchers;
import io.github.mikexliu.stack.guice.plugins.app.AppPlugin;
import io.github.mikexliu.stack.guice.plugins.app.metrics.MetricsPlugin;

public class TimedPlugin extends AppPlugin {

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
