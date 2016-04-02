package io.github.mikexliu.main;

import io.github.mikexliu.guice.modules.ServerExampleModule;
import io.github.mikexliu.stack.guice.plugins.back.scheduledservice.ServicesManagerModule;
import io.github.mikexliu.stack.guice.plugins.front.timed.TimedModule;
import io.github.mikexliu.stack.server.StackServer;

import javax.ws.rs.core.Response;

public class Main {
    public static void main(String[] args) throws Exception {
        new StackServer.Builder()
                .withTitle("server-example")
                .withDescription("server-example description")
                .withVersion("0.0.1-SNAPSHOT")
                .withApiPackageName("io.github.mikexliu.api")
                .withSwaggerEnabled()
                .withBackModule(ServicesManagerModule.class)
                .withFrontModule(TimedModule.class)
                .withFrontModule(ServerExampleModule.class)
                .withCorsEnabled()
                .withExceptionHandler(throwable -> Response.status(Response.Status.FORBIDDEN).build())
                .withPort(5454)
                .start();
    }
}
