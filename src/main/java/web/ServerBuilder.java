package web;

import java.net.URI;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.DispatcherType;
import javax.ws.rs.Path;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;

public class ServerBuilder {

    private URI endpoint;
    private Injector injector;

    public ServerBuilder withResourceEndpoint(final URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public ServerBuilder withInjector(final Injector injector) {
        this.injector = injector;
        return this;
    }

    public Server build() {
        try {
            // Workaround for resources from JAR files
            Resource.setDefaultUseCaches(false);

            buildSwagger();

            final HandlerList handlers = new HandlerList();
            handlers.addHandler(buildSwaggerUI());
            handlers.addHandler(buildContext());

            final Server server = new Server(endpoint.getPort());
            server.setHandler(handlers);
            return server;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Class<?>> getResources() {
        final Set<Key<?>> keys = this.injector.getAllBindings().keySet();
        final Set<Class<?>> resources = Sets.newHashSet();
        for (final Key<?> key : keys) {
            final Class<?> classType = key.getTypeLiteral().getRawType();
            if (classType.isAnnotationPresent(Path.class)) {
                resources.add(classType);
            }
        }
        return resources;
    }

    private void buildSwagger() {
        final BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setResourcePackage(Joiner.on(",").join(
                getResources().stream().map(Class::getPackage).map(Package::getName).collect(Collectors.toSet())));
        beanConfig.setScan(true);
        beanConfig.setBasePath("/");
        beanConfig.setDescription("Swagger");
        beanConfig.setTitle("Swagger");
    }

    private ContextHandler buildContext() {
        final Set<String> resources = Sets.newHashSet();
        resources.add(ApiListingResource.class.getPackage().getName());
        final String[] packages = resources.toArray(new String[resources.size()]);
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.packages(packages);

        final ServletHolder servletHolder = new ServletHolder(new ServletContainer(resourceConfig));
        final ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        servletContextHandler.addServlet(servletHolder, "/*");

        final Injector childInjector = injector.createChildInjector(new ServletModule() {
            @Override
            protected void configureServlets() {
                bind(GuiceContainer.class);

                final Map<String, String> parameters = Maps.newHashMap();
                parameters.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
                serve("/*").with(GuiceContainer.class, parameters);
            }
        });
        
        FilterHolder guiceFilter = new FilterHolder(childInjector.getInstance(GuiceFilter.class));
        servletContextHandler.addFilter(guiceFilter, "/api/*", EnumSet.allOf(DispatcherType.class));
        servletContextHandler.addServlet(DefaultServlet.class, "/");
        servletContextHandler.addEventListener(new GuiceServletContextListener() {
            @Override
            protected Injector getInjector() {
                return childInjector;
            }
        });

        return servletContextHandler;
    }

    private ContextHandler buildSwaggerUI() throws Exception {
        final ResourceHandler swaggerUIResourceHandler = new ResourceHandler();
        swaggerUIResourceHandler
                .setResourceBase(getClass().getClassLoader().getResource("swagger-ui").toURI().toString());
        final ContextHandler swaggerUIContext = new ContextHandler();
        swaggerUIContext.setContextPath("/docs/");
        swaggerUIContext.setHandler(swaggerUIResourceHandler);

        return swaggerUIContext;
    }
}
