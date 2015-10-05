package example;

import org.eclipse.jetty.server.Server;

import com.google.inject.Guice;
import com.google.inject.Injector;

import web.ServerBuilder;

public class Main {
    
    public static void main(String[] args) throws Exception {

        final MyModule myModule = new MyModule();
        final Injector injector = Guice.createInjector(myModule);

        final Server server = new ServerBuilder().withPort(5555).withInjector(injector).build();
        server.start();
    }
}
