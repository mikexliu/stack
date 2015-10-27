package web;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.DispatcherType;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;

public class Stack {

    private static final Logger log = LoggerFactory.getLogger(Stack.class);

    private static final Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(Stack.class.getResourceAsStream("/stack.properties"));

            Resource.setDefaultUseCaches(false);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load stack.properties", e);
        }
    }

    private final Server server;
    private final Injector injector;

    private final ResponseThrowableHandler responseThrowableHandler;
    
    public Stack(final Injector injector) {
        this(injector, null);
    }
    
    /**
     * 
     * @param injector
     * @param properties
     */
    public Stack(final Injector injector, final Properties properties) {
        if (properties != null) {
            Stack.properties.clear();
            Stack.properties.putAll(properties);
        }
        
        if (injector.getExistingBinding(Key.get(ResponseThrowableHandler.class)) == null) {
            responseThrowableHandler = new ResponseThrowableHandler() {

                @Override
                public Response handleThrowable(final Throwable throwable) {
                    log.warn("Internal Server Exception", throwable);
                    return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).build();
                }
            };
        } else {
            responseThrowableHandler = injector.getInstance(ResponseThrowableHandler.class);
        }

        this.injector = injector;

        this.server = new Server(Integer.parseInt(Stack.properties.getProperty("port")));
    }

    public void start() {
        try {
            final BeanConfig beanConfig = new BeanConfig();
            beanConfig.setVersion(Stack.properties.getProperty("version"));
            beanConfig.setResourcePackage(Joiner.on(",").join(
                    getResources().stream().map(Class::getPackage).map(Package::getName).collect(Collectors.toSet())));
            beanConfig.setScan(true);
            beanConfig.setBasePath("/");
            beanConfig.setTitle(Stack.properties.getProperty("swagger.title"));
            beanConfig.setDescription(Stack.properties.getProperty("swagger.description"));

            final HandlerList handlers = new HandlerList();
            handlers.addHandler(buildSwaggerContext());
            handlers.addHandler(buildContext());

            server.setHandler(handlers);
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Class<?>> getResources() {
        final Set<Key<?>> keys = injector.getAllBindings().keySet();
        final Set<Class<?>> resources = Sets.newHashSet();
        for (final Key<?> key : keys) {
            final Class<?> classType = key.getTypeLiteral().getRawType();
            if (classType.isAnnotationPresent(Path.class)) {
                resources.add(classType);
            }
        }
        return resources;
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
                bind(GuiceContainer.class).in(Scopes.SINGLETON);

                final Map<String, String> parameters = Maps.newHashMap();
                parameters.put(PackagesResourceConfig.PROPERTY_PACKAGES, ResponseThrowableMapper.class.getPackage()
                        .getName());
                parameters.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
                serve("/*").with(GuiceContainer.class, parameters);
                
                bind(ResponseThrowableMapper.class).toInstance(new ResponseThrowableMapper(responseThrowableHandler));
            }
        });

        final FilterHolder guiceFilter = new FilterHolder(childInjector.getInstance(GuiceFilter.class));
        servletContextHandler
                .addFilter(guiceFilter, String.format("/%s/*", Stack.properties.getProperty("api.prefix")),
                        EnumSet.allOf(DispatcherType.class));
        servletContextHandler.addServlet(DefaultServlet.class, "/");
        servletContextHandler.addEventListener(new GuiceServletContextListener() {
            @Override
            protected Injector getInjector() {
                return childInjector;
            }
        });

        return servletContextHandler;
    }

    private ContextHandler buildSwaggerContext() throws URISyntaxException {
        final ResourceHandler swaggerUIResourceHandler = new ResourceHandler();
        swaggerUIResourceHandler.setResourceBase(getClass().getClassLoader()
                .getResource(Stack.properties.getProperty("swagger.dist.folder")).toURI().toString());
        final ContextHandler swaggerUIContext = new ContextHandler();
        swaggerUIContext.setContextPath(String.format("/%s/", Stack.properties.getProperty("docs.prefix")));
        swaggerUIContext.setHandler(swaggerUIResourceHandler);

        return swaggerUIContext;
    }

    @Provider
    private static class ResponseThrowableMapper implements ExceptionMapper<Throwable> {

        private final ResponseThrowableHandler responseTHrowableHandler;

        public ResponseThrowableMapper(final ResponseThrowableHandler responseThrowableHandler) {
            this.responseTHrowableHandler = responseThrowableHandler;
        }

        @Override
        public Response toResponse(final Throwable throwable) {
            return responseTHrowableHandler.handleThrowable(throwable);
        }
    }

    /**
     * Interface to provide a custom exception handler for
     * JsonProcessingExceptions
     */
    public static interface ResponseThrowableHandler {
        public Response handleThrowable(final Throwable throwable);
    }
}
