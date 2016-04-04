package io.github.mikexliu.stack.server;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import io.github.mikexliu.stack.guice.modules.apis.ContainersModule;
import io.github.mikexliu.stack.guice.modules.apis.ResourcesModule;
import io.github.mikexliu.stack.guice.modules.swagger.StackServletModule;
import io.github.mikexliu.stack.guice.modules.swagger.handler.exception.ThrowableResponseHandler;
import io.github.mikexliu.stack.guice.plugins.app.AppPlugin;
import io.github.mikexliu.stack.guice.plugins.stack.StackPlugin;
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
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO: publish this to central repository.
 * https://maven.apache.org/guides/mini/guide-central-repository-upload.html
 */
public class StackServer {

    private static final Logger log = LoggerFactory.getLogger(StackServer.class);

    private static final String SWAGGER_CONTEXT_PATH = "/docs/";
    private static final String JERSEY_CONTEXT_PATH = "api";

    private final Server server;
    private final Injector backInjector;

    private final Builder builder;

    private StackServer(final Builder builder) throws Exception {
        // https://www.javacodegeeks.com/2013/10/swagger-make-developers-love-working-with-your-rest-api.html
        Resource.setDefaultUseCaches(false);

        this.builder = builder;

        // stack modules
        final List<Module> appPlugins = new LinkedList<>();
        appPlugins.addAll(builder.appModules);
        for (final Class<? extends AppPlugin> frontModuleClass : this.builder.appPlugins) {
            appPlugins.add(frontModuleClass.newInstance());
        }

        appPlugins.add(new ContainersModule(builder.apiPackageNames));
        final Injector frontInjector = Guice.createInjector(appPlugins);

        // meta modules
        final Collection<StackPlugin> stackPlugins = new LinkedList<>();
        for (final Class<? extends StackPlugin> backModuleClass : this.builder.stackPlugins) {
            stackPlugins.add(backModuleClass.getConstructor(Injector.class).newInstance(frontInjector));
        }
        stackPlugins.add(new ResourcesModule(builder.apiPackageNames, frontInjector));
        this.backInjector = frontInjector.createChildInjector(stackPlugins);

        this.server = new Server(builder.port);
    }

    public void start() throws Exception {
        final HandlerList handlers = new HandlerList();

        if (builder.swaggerEnabled) {
            handlers.addHandler(buildSwaggerContext());
        }

        handlers.addHandler(buildJerseyContext());
        server.setHandler(handlers);
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    private Set<Class<?>> getResources() {
        final Set<Key<?>> keys = backInjector.getAllBindings().keySet();
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

        final Injector servletInjector = backInjector.createChildInjector(new StackServletModule(builder.corsEnabled, builder.throwableResponseHandler));

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

        private final List<String> apiPackageNames;
        private final List<Module> appModules;
        private final List<Class<? extends AppPlugin>> appPlugins;
        private final List<Class<? extends StackPlugin>> stackPlugins;

        private String title = "stack";
        private String version = "0.0.1";
        private String description = "stack makes it easy to generate rest endpoints.";

        private int port = 5555;
        private boolean corsEnabled = false;
        private ThrowableResponseHandler throwableResponseHandler = throwable -> {
            log.warn("Server encountered exception", throwable);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        };

        private boolean swaggerEnabled = false;
        private String swaggerUIDirectory = "swagger-ui";

        public Builder() {
            this.apiPackageNames = new LinkedList<>();
            this.appModules = new LinkedList<>();
            this.appPlugins = new LinkedList<>();
            this.stackPlugins = new LinkedList<>();
        }

        /**
         *
         * @param port
         * @return
         */
        public Builder withPort(final int port) {
            this.port = port;
            return this;
        }

        public Builder withCorsEnabled() {
            this.corsEnabled = true;
            return this;
        }

        public Builder withExceptionHandler(final ThrowableResponseHandler throwableResponseHandler) {
            this.throwableResponseHandler = throwableResponseHandler;
            return this;
        }

        /**
         * Package name of @Api implementations
         * @param packageName
         * @return
         */
        public Builder withApiPackageName(final String packageName) {
            this.apiPackageNames.add(packageName);
            return this;
        }

        /**
         *
         * @param appModule
         * @return
         */
        public Builder withAppModule(final Module appModule) {
            this.appModules.add(appModule);
            return this;
        }

        /**
         *
         * @param appPlugin
         * @return
         */
        public Builder withAppPlugin(final Class<? extends AppPlugin> appPlugin) {
            this.appPlugins.add(appPlugin);
            return this;
        }

        /**
         *
         * @param stackPlugin
         * @return
         */
        public Builder withStackPlugin(final Class<? extends StackPlugin> stackPlugin) {
            this.stackPlugins.add(stackPlugin);
            return this;
        }

        /**
         *
         * @return
         */
        public Builder withSwaggerEnabled() {
            this.swaggerEnabled = true;
            return this;
        }

        /**
         * Enables Swagger and sets the swagger-ui directory
         * @param swaggerUIDirectory
         * @return
         */
        public Builder withSwaggerUiDirectory(final String swaggerUIDirectory) {
            this.withSwaggerEnabled();
            this.swaggerUIDirectory = swaggerUIDirectory;
            return this;
        }

        public Builder withTitle(final String title) {
            this.title = title;
            return this;
        }

        public Builder withVersion(final String version) {
            this.version = version;
            return this;
        }

        public Builder withDescription(final String description) {
            this.description = description;
            return this;
        }

        public StackServer build() throws Exception {
            Preconditions.checkArgument(!apiPackageNames.isEmpty(), "No api package name specified; cannot find api classes.");
            stackPlugins.forEach(stackPlugin -> {
                try {
                    stackPlugin.getConstructor(Injector.class);
                } catch (NoSuchMethodException e) {
                    Preconditions.checkState(false, String.format("Constructor public %s(Injector injector) does not exist.",
                            stackPlugin.getSimpleName()));
                }
            });

            return new StackServer(this);
        }

        public void start() throws Exception {
            new StackServer(this).start();
        }
    }
}