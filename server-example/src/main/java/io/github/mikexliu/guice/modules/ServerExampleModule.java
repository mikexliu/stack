package io.github.mikexliu.guice.modules;

import io.github.mikexliu.stack.guice.plugins.StackPlugin;

public class ServerExampleModule extends StackPlugin {

    @Override
    protected void configure() {
        install(new ScheduledServiceModule());
        install(new ServiceModule());
    }
}
