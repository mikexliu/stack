package io.github.stack.server;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import io.github.stack.guice.modules.swagger.StackServletModule;
import io.github.stack.guice.modules.swagger.handler.exception.ThrowableResponseHandler;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
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

import javax.servlet.DispatcherType;
import javax.ws.rs.Path;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO: publish this to central repository.
 * https://maven.apache.org/guides/mini/guide-central-repository-upload.html
 */
public class Stack {
    
    private static final Logger log = LoggerFactory.getLogger(Stack.class);
    
    private static final String SWAGGER_CONTEXT_PATH = "/docs/";
    private static final String JERSEY_CONTEXT_PATH = "api";
    
    private final Server server;
    private final Injector injector;
    
    private final Builder builder;
    
    private Stack(final Builder builder) throws Exception {
        // https://www.javacodegeeks.com/2013/10/swagger-make-developers-love-working-with-your-rest-api.html
        Resource.setDefaultUseCaches(false);
        
        this.builder = builder;
        this.injector = Guice.createInjector(this.builder.modules);
        this.server = new Server(builder.port);
    }
    
    public void start() throws Exception {
        final HandlerList handlers = new HandlerList();
        
        handlers.addHandler(buildSwaggerContext());
        handlers.addHandler(buildJerseyContext());
        server.setHandler(handlers);
        server.start();
        
        log.info(Stack.class + " Started");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!server.isStopping() && !server.isStopped()) {
                try {
                    server.stop();
                } catch (Exception e) {
                    log.warn("Failed to stop " + Stack.class, e);
                }
            }
            
            log.info(Stack.class + " Stopped");
        }));
    }
    
    public void stop() throws Exception {
        server.stop();
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
    
    private ContextHandler buildJerseyContext() {
        final Set<String> resources = Sets.newHashSet();
        resources.add(ApiListingResource.class.getPackage().getName());
        final String[] packages = resources.toArray(new String[resources.size()]);
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.packages(packages);
        
        final ServletHolder servletHolder = new ServletHolder(new ServletContainer(resourceConfig));
        final ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        servletContextHandler.addServlet(servletHolder, "/*");
        servletContextHandler.addServlet(DefaultServlet.class, "/");
        
        final Injector servletInjector = injector.createChildInjector(new StackServletModule(builder.throwableResponseHandler));
        
        final FilterHolder guiceFilter = new FilterHolder(servletInjector.getInstance(GuiceFilter.class));
        servletContextHandler.addFilter(guiceFilter, String.format("/%s/*", JERSEY_CONTEXT_PATH), EnumSet.allOf(DispatcherType.class));
        servletContextHandler.addEventListener(new GuiceServletContextListener() {
            @Override
            protected Injector getInjector() {
                return servletInjector;
            }
        });
        
        return servletContextHandler;
    }
    
    // TODO: add swagger-ui as maven dependency
    private ContextHandler buildSwaggerContext() throws URISyntaxException {
        final BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion(this.builder.version);
        beanConfig.setTitle(this.builder.title);
        beanConfig.setDescription(this.builder.description);
        beanConfig.setBasePath("/");
        beanConfig.setResourcePackage(Joiner.on(",").join(getResources().stream()
                .map(Class::getPackage)
                .map(Package::getName)
                .collect(Collectors.toSet())));
        beanConfig.setScan(true);
        
        final ResourceHandler swaggerUIResourceHandler = new ResourceHandler();
        swaggerUIResourceHandler.setResourceBase(getClass().getClassLoader().getResource(builder.swaggerUIDirectory).toURI().toString());
        final ContextHandler swaggerUIContext = new ContextHandler();
        swaggerUIContext.setContextPath(SWAGGER_CONTEXT_PATH);
        swaggerUIContext.setHandler(swaggerUIResourceHandler);
        
        return swaggerUIContext;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        
        private final Set<Module> modules;
        
        private String title = "stack";
        private String version = "0.0.1";
        private String description = "sample description";
        
        private int port = 5555;
        private ThrowableResponseHandler throwableResponseHandler = null;
        
        private String swaggerUIDirectory = "swagger-ui";
        
        public Builder() {
            this.modules = new HashSet<>();
        }
        
        /**
         * Default port: 5555
         *
         * @param port
         * @return
         */
        public Builder withPort(final int port) {
            this.port = port;
            return this;
        }
        
        /**
         * Defines how to handle Throwable while handling a request.
         * No default implementation
         *
         * @param throwableResponseHandler
         * @return
         */
        public Builder withExceptionHandler(final ThrowableResponseHandler throwableResponseHandler) {
            this.throwableResponseHandler = throwableResponseHandler;
            return this;
        }
        
        /**
         * @param modules
         * @return
         */
        public Builder withModule(final Module... modules) {
            Arrays.asList(modules).forEach(this.modules::add);
            return this;
        }
        
        /**
         * Enables Swagger and sets the swagger-ui title
         * Default: "stack"
         *
         * @param title
         * @return
         */
        public Builder withTitle(final String title) {
            this.title = title;
            return this;
        }
        
        /**
         * Enables Swagger and sets the swagger-ui version
         * Default: "0.0.1"
         *
         * @param version
         * @return
         */
        public Builder withVersion(final String version) {
            this.version = version;
            return this;
        }
        
        /**
         * Enables Swagger and sets the swagger-ui description
         * Default: "sample description"
         *
         * @param description
         * @return
         */
        public Builder withDescription(final String description) {
            this.description = description;
            return this;
        }
        
        /**
         * @return
         * @throws Exception
         */
        public Stack build() throws Exception {
            return new Stack(this);
        }
    }
}