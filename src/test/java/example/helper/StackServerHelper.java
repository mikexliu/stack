package example.helper;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import example.MyItem;
import stack.server.Stack;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class StackServerHelper {

    private final Stack stack;

    public StackServerHelper(final int port) throws Exception {
        final Injector injector = Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
                final Map<String, MyItem> itemsv1 = new HashMap<>();
                bind(new TypeLiteral<Map<String, MyItem>>() {
                }).annotatedWith(Names.named("itemsv1")).toInstance(itemsv1);

                final Map<String, MyItem> itemsv2 = new HashMap<>();
                bind(new TypeLiteral<Map<String, MyItem>>() {
                }).annotatedWith(Names.named("itemsv2")).toInstance(itemsv2);

                final Map<String, MyItem> itemsv3 = new HashMap<>();
                bind(new TypeLiteral<Map<String, MyItem>>() {
                }).annotatedWith(Names.named("itemsv3")).toInstance(itemsv3);
            }
        });

        final Properties portOverride = new Properties();
        portOverride.put("port", port);

        stack = new Stack(injector, portOverride);
    }

    public void start() throws Exception {
        stack.start();
    }

    public void stop() throws Exception {
        stack.stop();
    }
}
