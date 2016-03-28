package io.github.mikexliu.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import io.github.mikexliu.scheduledservice.GenericScheduledService;

/**
 * Created by mliu on 3/28/16.
 */
public class ScheduledServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GenericScheduledService.class).in(Scopes.SINGLETON);
    }
}
