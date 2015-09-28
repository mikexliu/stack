package example;


import java.io.IOException;

import org.glassfish.grizzly.http.server.HttpServer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory;

public class Main {
    public static void main(String[] args) throws IllegalArgumentException, IOException {

        final MyModule myModule = new MyModule();
        final Injector injector = Guice.createInjector(myModule);
        
        final ResourceConfig rc = new DefaultResourceConfig(MyResource.class);
        IoCComponentProviderFactory ioc = new GuiceComponentProviderFactory(rc, injector);
        
        final HttpServer server = GrizzlyServerFactory.createHttpServer("http://localhost:5555", rc, ioc);
        server.start();
        System.out.println("Press any key to exit...");
        System.in.read();
    }
}
