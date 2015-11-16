package example;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import stack.server.Stack;

import java.util.HashMap;
import java.util.Map;

/**
 * Example main method
 */
public class Main {

    public static void main(String[] args) throws Exception {
        final Injector injector = Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
                final Map<String, MyItem> itemsv1 = new HashMap<>();
                bind(new TypeLiteral<Map<String, MyItem>>() {
                }).annotatedWith(Names.named("itemsv1")).toInstance(itemsv1);

                final Map<String, MyItem> itemsv2 = new HashMap<>();
                bind(new TypeLiteral<Map<String, MyItem>>() {
                }).annotatedWith(Names.named("itemsv2")).toInstance(itemsv2);
            }
        });

        new Stack(injector).start();
    }
}
