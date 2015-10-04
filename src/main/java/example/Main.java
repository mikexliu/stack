package example;

import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.glassfish.grizzly.http.server.HttpServer;

import com.google.inject.Guice;
import com.google.inject.Injector;

import web.ServerBuilder;
import web.SwaggerServerBuilder;

public class Main {
    public static void main(String[] args) throws Exception {

        final MyModule myModule = new MyModule();
        final Injector injector = Guice.createInjector(myModule);
        
        final URI endpoint = new URI("http://localhost:5555");
        
//        final HttpServer server = new ServerBuilder()
//            .withEndpoint(endpoint)
//            .withInjector(injector)
//            .build();

        //server.start();
        
        // TODO: add authentication to swagger usage
        final boolean withSwagger = true;
        final Server swaggerServer;
        if (withSwagger) {
            swaggerServer = new SwaggerServerBuilder()
                    .withPort(endpoint.getPort())
                    .withResourceEndpoint(endpoint)
                    .withInjector(injector)
                    .build();
            swaggerServer.start();
            System.out.println("Started Swagger");
        }
        
        try {
            System.out.println("Press any key to exit...");
            System.in.read();
        } finally {
//            if (server.isStarted()) {
//                server.stop();
//            }

            if (swaggerServer != null && swaggerServer.isStarted()) {
                swaggerServer.stop();
            }
        }
    }
}
