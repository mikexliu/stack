package example;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import web.Stack;

public class Main {

    public static void main(String[] args) throws Exception {
        final Injector injector = Guice.createInjector(new AbstractModule() {

            /**
             * Inject the data map into {@link MyContainer}.
             */
            @Override
            protected void configure() {
                final Map<String, MyItem> items = new HashMap<>();
                bind(new TypeLiteral<Map<String, MyItem>>() {
                }).annotatedWith(Names.named("items")).toInstance(items);
            }
        });

        new Stack(injector).start();
    }
}
