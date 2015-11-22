package example.helper;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import example.data.MyItem;
import stack.server.Stack;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class StackServerHelper {

    private final Stack stack;

    public StackServerHelper(final int port, final String packages) throws Exception {
        final Injector injector = Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
                final Map<String, MyItem> itemsv1 = new HashMap<>();
                bind(new TypeLiteral<Map<String, MyItem>>() {
                }).annotatedWith(Names.named("items")).toInstance(itemsv1);
            }
        });

        final Properties properties = new Properties();
        properties.put("port", port);

        // TODO: put this in a addPackage(..)
        // TODO: put a addClass(..)
        properties.put("packages", packages);

        stack = new Stack(injector, properties);
    }

    public void start() throws Exception {
        stack.start();
    }

    public void stop() throws Exception {
        stack.stop();
    }
}
