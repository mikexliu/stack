package example;

import java.net.URI;

import org.eclipse.jetty.server.Server;

import com.google.inject.Guice;
import com.google.inject.Injector;

import web.ServerBuilder;

public class Main {
    public static void main(String[] args) throws Exception {

        final MyModule myModule = new MyModule();
        final Injector injector = Guice.createInjector(myModule);

        final URI endpoint = new URI("http://localhost:5555");

        final Server swaggerServer = new ServerBuilder().withResourceEndpoint(endpoint).withInjector(injector).build();
        swaggerServer.start();

        try {
            System.out.println("Press any key to exit...");
            System.in.read();
        } finally {
            if (swaggerServer != null && swaggerServer.isStarted()) {
                swaggerServer.stop();
            }
        }
    }
}
