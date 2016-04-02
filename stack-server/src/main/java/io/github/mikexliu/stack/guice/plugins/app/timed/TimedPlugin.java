package io.github.mikexliu.stack.guice.plugins.app.timed;

import com.google.inject.matcher.Matchers;
import io.github.mikexliu.stack.guice.plugins.app.AppPlugin;

public class TimedPlugin extends AppPlugin {

    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Timed.class), new TimedInterceptor());
    }
}
