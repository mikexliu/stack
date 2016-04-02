package io.github.mikexliu.main;

import io.github.mikexliu.guice.modules.ScheduledServiceModule;
import io.github.mikexliu.guice.modules.ServiceModule;
import io.github.mikexliu.stack.guice.plugins.back.scheduledservice.ServicesManagerModule;
import io.github.mikexliu.stack.guice.plugins.front.timed.TimedModule;
import io.github.mikexliu.stack.server.StackServer;

public class Main {
    public static void main(String[] args) throws Exception {
        new StackServer.Builder()
                .withTitle("server-example")
                .withDescription("server-example description")
                .withVersion("0.0.1-SNAPSHOT")
                .withApiPackageName("io.github.mikexliu.api")
                .withBackModule(ServicesManagerModule.class)
                .withFrontModule(TimedModule.class)
                .withFrontModule(ScheduledServiceModule.class)
                .withFrontModule(ServiceModule.class)
                .withCorsEnabled()
                .withPort(5454)
                .start();
    }
}
