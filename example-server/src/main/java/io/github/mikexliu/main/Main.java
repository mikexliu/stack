package io.github.mikexliu.main;

import io.github.mikexliu.guice.modules.ServerExampleModule;
import io.github.mikexliu.stack.guice.plugins.metrics.timed.TimedPlugin;
import io.github.mikexliu.stack.guice.plugins.persistence.filesystem.FileSystemPlugin;
import io.github.mikexliu.stack.guice.plugins.services.scheduledservice.ScheduledServicePlugin;
import io.github.mikexliu.stack.server.StackServer;

import javax.ws.rs.core.Response;

public class Main {

    public static void main(String[] args) throws Exception {
        StackServer.builder()
                .withTitle("server-example")
                .withDescription("server-example description")
                .withVersion("0.0.1-SNAPSHOT")
                .withApiPackageName("io.github.mikexliu.api")
                .withSwaggerUiDirectory("swagger-ui")
                .withSwaggerEnabled()
                .withPlugin(ScheduledServicePlugin.class)
                .withPlugin(FileSystemPlugin.class)
                .withPlugin(TimedPlugin.class)
                .withModule(new ServerExampleModule())
                .withCorsEnabled()
                .withExceptionHandler(throwable ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .build())
                .withPort(5454)
                .build()
                .start();
    }
}
