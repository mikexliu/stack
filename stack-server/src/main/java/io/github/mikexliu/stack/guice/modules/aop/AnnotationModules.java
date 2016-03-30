package io.github.mikexliu.stack.guice.modules.aop;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import io.github.mikexliu.stack.guice.aop.timed.Timed;
import io.github.mikexliu.stack.guice.aop.timed.TimedInterceptor;

/**
 * Created by mliu on 3/27/16.
 */
public class AnnotationModules extends AbstractModule {

    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Timed.class), new TimedInterceptor());
    }
}
