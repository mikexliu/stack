package io.github.mikexliu.stack.guice.plugins.front.timed;

import com.google.inject.matcher.Matchers;
import io.github.mikexliu.stack.guice.plugins.front.FrontModule;

public class TimedModule extends FrontModule {

    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Timed.class), new TimedInterceptor());
    }
}
