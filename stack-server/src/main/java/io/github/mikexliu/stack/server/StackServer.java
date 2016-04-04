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
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.Arrays;
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
    private final Injector stackInjector;

    private final Builder builder;

    private StackServer(final Builder builder) throws Exception {
        // https://www.javacodegeeks.com/2013/10/swagger-make-developers-love-working-with-your-rest-api.html
        Resource.setDefaultUseCaches(false);

        this.builder = builder;

        // stack modules
        final Collection<Module> appPlugins = new LinkedList<>();
        final Collection<StackPlugin> stackPlugins = new LinkedList<>();

        appPlugins.addAll(builder.appModules);
        for (final Class<? extends AppPlugin> appPluginClass : this.builder.appPlugins) {
            final AppPlugin appPlugin = appPluginClass.newInstance();
            appPlugins.add(appPlugin);

            this.builder.stackPlugins.addAll(appPlugin.getStackPluginDependencies());
        }

        appPlugins.add(new ContainersModule(builder.apiPackageNames));
        final Injector frontInjector = Guice.createInjector(appPlugins);

        // meta modules
        for (final Class<? extends StackPlugin> stackPluginClass : this.builder.stackPlugins) {
            stackPlugins.add(stackPluginClass.getConstructor(Injector.class).newInstance(frontInjector));
        }
        stackPlugins.add(new ResourcesModule(builder.apiPackageNames, frontInjector));
        this.stackInjector = frontInjector.createChildInjector(stackPlugins);

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

        log.info(StackServer.class + " Started");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (!server.isStopping() && !server.isStopped()) {
                    try {
                        server.stop();
                    } catch (Exception e) {
                        log.warn("Failed to stop " + StackServer.class, e);
                    }
                }

                log.info(StackServer.class + " Stopped");
            }
        });
    }

    public void stop() throws Exception {
        server.stop();
    }

    private Set<Class<?>> getResources() {
        final Set<Key<?>> keys = stackInjector.getAllBindings().keySet();
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

        final Injector servletInjector = stackInjector.createChildInjector(new StackServletModule(builder.corsEnabled, builder.throwableResponseHandler));

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
        private String description = "sample description";

        private int port = 5555;
        private boolean corsEnabled = false;
        private ThrowableResponseHandler throwableResponseHandler = null;

        private boolean swaggerEnabled = false;
        private String swaggerUIDirectory = "swagger-ui";

        public Builder() {
            this.apiPackageNames = new LinkedList<>();
            this.appModules = new LinkedList<>();
            this.appPlugins = new LinkedList<>();
            this.stackPlugins = new LinkedList<>();
        }

        /**
         * Default port: 5555
         * @param port
         * @return
         */
        public Builder withPort(final int port) {
            this.port = port;
            return this;
        }

        /**
         * Default: disabled
         * @return
         */
        public Builder withCorsEnabled() {
            this.corsEnabled = true;
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
         * Package name of @Api implementations
         * No default implementation
         *
         * @param packageName
         * @return
         */
        public Builder withApiPackageName(final String packageName) {
            this.apiPackageNames.add(packageName);
            return this;
        }

        /**
         * @param appModule
         * @return
         */
        public Builder withAppModule(final Module appModule) {
            this.appModules.add(appModule);
            return this;
        }

        /**
         * @param appPlugin
         * @return
         */
        public Builder withAppPlugin(final Class<? extends AppPlugin> appPlugin) {
            this.appPlugins.add(appPlugin);
            return this;
        }

        /**
         * @param appPlugins
         * @return
         */
        public Builder withAppLugins(final Class<? extends AppPlugin>... appPlugins) {
            this.appPlugins.addAll(Arrays.asList(appPlugins));
            return this;
        }

        /**
         * @param stackPlugin
         * @return
         */
        public Builder withStackPlugin(final Class<? extends StackPlugin> stackPlugin) {
            this.stackPlugins.add(stackPlugin);
            return this;
        }

        /**
         * @param stackPlugins
         * @return
         */
        public Builder withStackPlugins(final Class<? extends StackPlugin>... stackPlugins) {
            this.stackPlugins.addAll(Arrays.asList(stackPlugins));
            return this;
        }

        /**
         * Enables Swagger using the default swagger-ui resource
         * Default: disabled
         *
         * @return
         */
        public Builder withSwaggerEnabled() {
            this.swaggerEnabled = true;
            return this;
        }

        /**
         * Enables Swagger and sets the swagger-ui directory
         * Default: "swagger-ui"
         *
         * @param swaggerUIDirectory
         * @return
         */
        public Builder withSwaggerUiDirectory(final String swaggerUIDirectory) {
            this.withSwaggerEnabled();
            this.swaggerUIDirectory = swaggerUIDirectory;
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
            this.withSwaggerEnabled();
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
            this.withSwaggerEnabled();
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
            this.withSwaggerEnabled();
            this.description = description;
            return this;
        }

        /**
         *
         * @return
         * @throws Exception
         */
        public StackServer build() throws Exception {
            Preconditions.checkArgument(!apiPackageNames.isEmpty(), "No api package name specified; cannot find api classes.");

            appPlugins.forEach(appPlugin -> {
                try {
                    // TODO: verify AppPlugin constructor
                    appPlugin.newInstance().getStackPluginDependencies().forEach(stackPlugin -> verifyStackPluginClass(stackPlugin));
                } catch (InstantiationException | IllegalAccessException e) {
                    Preconditions.checkState(false, appPlugin + " could not be instantiated.");
                }
            });

            stackPlugins.forEach(stackPlugin -> verifyStackPluginClass(stackPlugin));

            return new StackServer(this);
        }

        private void verifyStackPluginClass(final Class<? extends StackPlugin> stackPlugin) {
            try {
                Preconditions.checkState(!Modifier.isAbstract(stackPlugin.getModifiers()), String.format("%s is abstract.", stackPlugin));
                Preconditions.checkState(Modifier.isPublic(stackPlugin.getModifiers()), String.format("%s is not public.", stackPlugin));
                final Constructor<? extends StackPlugin> constructor = stackPlugin.getConstructor(Injector.class);
                Preconditions.checkState(Modifier.isPublic(constructor.getModifiers()),
                        String.format("Constructor %s(Injector injector) is not public.", stackPlugin.getSimpleName()));
            } catch (NoSuchMethodException e) {
                Preconditions.checkState(false, String.format("Constructor %s(Injector injector) does not exist.",
                        stackPlugin.getSimpleName()));
            }
        }
    }
}