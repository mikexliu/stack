package io.github.mikexliu.main;

import io.github.mikexliu.guice.modules.ServerExampleModule;
import io.github.mikexliu.stack.guice.plugins.stack.scheduledservice.ScheduledServiceAutoStartPlugin;
import io.github.mikexliu.stack.guice.plugins.stack.scheduledservice.ScheduledServiceManagerPlugin;
import io.github.mikexliu.stack.guice.plugins.app.timed.TimedPlugin;
import io.github.mikexliu.stack.server.StackServer;

import javax.ws.rs.core.Response;

public class Main {

    public static void main(String[] args) throws Exception {
        StackServer.builder()
                .withTitle("server-example")
                .withDescription("server-example description")
                .withVersion("0.0.1-SNAPSHOT")
                .withApiPackageName("io.github.mikexliu.api")
                .withSwaggerEnabled()
                .withStackPlugin(ScheduledServiceManagerPlugin.class)
                .withStackPlugin(ScheduledServiceAutoStartPlugin.class)
                .withAppPlugin(TimedPlugin.class)
                .withAppModule(new ServerExampleModule())
                .withCorsEnabled()
                .withExceptionHandler(throwable ->
                        Response.status(Response.Status.NOT_FOUND)
                                .entity("Example Server Resource Not Found")
                                .build())
                .withPort(5454)
                .build()
                .start();
    }
}