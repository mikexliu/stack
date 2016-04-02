package io.github.mikexliu.guice.modules;

import com.google.inject.Scopes;
import io.github.mikexliu.scheduledservice.AgingService;
import io.github.mikexliu.stack.guice.plugins.app.AppPlugin;

public class ScheduledServiceModule extends AppPlugin {

    @Override
    protected void configure() {
        bind(AgingService.class).in(Scopes.SINGLETON);
    }
}
