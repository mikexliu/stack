package io.github.mikexliu.guice.modules;

import io.github.mikexliu.stack.guice.plugins.app.AppPlugin;

public class ServerExampleModule extends AppPlugin {

    @Override
    protected void configure() {
        install(new ScheduledServiceModule());
        install(new ServiceModule());
    }
}
