package io.github.mikexliu.main;

import io.github.mikexliu.guice.modules.ScheduledServiceModule;
import io.github.mikexliu.guice.modules.ServiceModule;
import io.github.mikexliu.stack.server.StackServer;

/**
 * Created by mliu on 3/27/16.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        new StackServer.Builder()
                .withTitle("server-example")
                .withDescription("server-example description")
                .withVersion("0.0.1-SNAPSHOT")
                .withApiPackageName("io.github.mikexliu.api")
                .withModules(new ScheduledServiceModule(), new ServiceModule())
                .withPort(5454)
                .start();
    }
}
