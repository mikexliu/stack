package io.github.mikexliu.stack.guice.plugins.front.timed;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * Created by mliu on 3/27/16.
 */
public class TimedModule extends AbstractModule {

    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Timed.class), new TimedInterceptor());
    }
}
