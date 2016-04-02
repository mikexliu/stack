package io.github.mikexliu.guice.modules;

import com.google.inject.Scopes;
import io.github.mikexliu.scheduledservice.AgingService;
import io.github.mikexliu.stack.guice.plugins.front.FrontModule;

/**
 * Created by mliu on 3/28/16.
 */
public class ScheduledServiceModule extends FrontModule {

    @Override
    protected void configure() {
        bind(AgingService.class).in(Scopes.SINGLETON);
    }
}
