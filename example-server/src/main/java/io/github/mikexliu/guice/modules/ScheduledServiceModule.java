package io.github.mikexliu.guice.modules;

import com.google.inject.Scopes;
import io.github.mikexliu.scheduledservice.RandomFileSystemService;
import io.github.mikexliu.stack.guice.plugins.StackPlugin;

public class ScheduledServiceModule extends StackPlugin {

    @Override
    protected void configure() {
        // bind(AgingService.class).in(Scopes.SINGLETON);
        bind(RandomFileSystemService.class).in(Scopes.SINGLETON);
    }
}
