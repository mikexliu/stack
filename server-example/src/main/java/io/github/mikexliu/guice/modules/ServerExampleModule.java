package io.github.mikexliu.guice.modules;

import io.github.mikexliu.stack.guice.plugins.front.FrontModule;

public class ServerExampleModule extends FrontModule {

    @Override
    protected void configure() {
        install(new ScheduledServiceModule());
        install(new ServiceModule());
    }
}
