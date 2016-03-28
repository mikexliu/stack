package io.github.mikexliu;

import io.github.mikexliu.guice.modules.ScheduledServiceModule;
import io.github.mikexliu.stack.server.StackServer;

/**
 * Created by mliu on 3/27/16.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        new StackServer.Builder()
                .withTitle("server-example")
                .withDescription("server-example description")
                .withPackageName("io.github.mikexliu.api")
                .withModule(new ScheduledServiceModule())
                .withPort(5454)
                .build()
                .start();
    }
}
