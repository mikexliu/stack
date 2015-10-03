package web;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import javax.ws.rs.Path;

import org.glassfish.grizzly.http.server.HttpServer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory;

public class ServerBuilder {

    private URI endpoint;
    private Injector injector;

    public ServerBuilder withEndpoint(final URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public ServerBuilder withInjector(final Injector injector) {
        this.injector = injector;
        return this;
    }

    public HttpServer build() {
        Preconditions.checkNotNull(this.endpoint);
        Preconditions.checkNotNull(this.injector);

        final Set<Key<?>> keys = this.injector.getAllBindings().keySet();
        final Set<Class<?>> resources = Sets.newHashSet();
        for (final Key<?> key : keys) {
            final Class<?> classType = key.getTypeLiteral().getRawType();
            if (classType.isAnnotationPresent(Path.class)) {
                resources.add(classType);
            }
        }

        final ResourceConfig rc = new DefaultResourceConfig(resources);
        rc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
        final IoCComponentProviderFactory ioc = new GuiceComponentProviderFactory(rc, injector);

        try {
            return GrizzlyServerFactory.createHttpServer(
                    String.format("%s://%s:%d", endpoint.getScheme(), endpoint.getHost(), endpoint.getPort()), rc, ioc);
        } catch (IllegalArgumentException | NullPointerException | IOException e) {
            throw new RuntimeException("Failed to create HttpServer", e);
        }
    }
}
