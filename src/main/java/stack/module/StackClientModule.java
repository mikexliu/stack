package stack.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import stack.client.StackClient;

import java.net.URL;
import java.util.Map;

/**
 * TODO: if container exists in the current injector, then we should go directly
 * to the container instead of through the rest api
 */
public class StackClientModule extends AbstractModule {

    final Map<String, URL> nameToEndpoint;

    /**
     * Maps from a name to a StackClient. StackClients can be injected with
     * <pre>
     * {@code
     *
     * @param nameToEndpoint
     * @Inject
     * @Named("name") StackClient client;
     * }
     * </pre>
     */
    public StackClientModule(final Map<String, URL> nameToEndpoint) {
        this.nameToEndpoint = nameToEndpoint;
    }

    @Override
    protected void configure() {
        for (final Map.Entry<String, URL> entry : nameToEndpoint.entrySet()) {
            final String name = entry.getKey();
            final URL endpoint = entry.getValue();

            final StackClient stackClient = new StackClient(endpoint.getProtocol(), endpoint.getHost(), endpoint.getPort());
            bind(StackClient.class).annotatedWith(Names.named(name)).toInstance(stackClient);
        }
    }
}
