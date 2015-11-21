package example.helper;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import stack.client.StackClient;
import stack.module.StackClientModule;

import java.net.URL;
import java.util.Map;

public class StackClientHelper {

    private final Injector injector;

    public StackClientHelper(final Map<String, URL> nameToEndpoint) {
        this.injector = Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
                final StackClientModule module = new StackClientModule(nameToEndpoint);
                install(module);
            }
        });
    }

    public StackClient getClient(final String name) {
        return injector.getInstance(Key.get(StackClient.class, Names.named(name)));
    }
}
