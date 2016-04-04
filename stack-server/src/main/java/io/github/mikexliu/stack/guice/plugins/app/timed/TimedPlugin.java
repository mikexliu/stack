package io.github.mikexliu.stack.guice.plugins.app.timed;

import com.google.inject.matcher.Matchers;
import io.github.mikexliu.stack.guice.plugins.app.AppPlugin;
import io.github.mikexliu.stack.guice.plugins.stack.StackPlugin;
import io.github.mikexliu.stack.guice.plugins.stack.metrics.MetricsPlugin;

import java.util.HashSet;
import java.util.Set;

public class TimedPlugin extends AppPlugin {

    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Timed.class), new TimedInterceptor());
    }

    @Override
    public Set<Class<? extends StackPlugin>> getStackPluginDependencies() {
        final Set<Class<? extends StackPlugin>> stackPluginDependencies = new HashSet<>();
        stackPluginDependencies.add(MetricsPlugin.class);
        return stackPluginDependencies;
    }
}
