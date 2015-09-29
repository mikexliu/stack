package example;

import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;

import web.ServerBuilder;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {
    public static void main(String[] args) throws Exception {

        final MyModule myModule = new MyModule();
        final Injector injector = Guice.createInjector(myModule);

        final HttpServer server = new ServerBuilder()
            .withEndpoint(new URI("http://localhost:5555"))
            .withInjector(injector)
            .build();

        server.start();
        System.out.println("Press any key to exit...");
        System.in.read();
    }
}
