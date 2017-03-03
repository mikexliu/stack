package io.github.stack.guice.plugins.services.scheduledservice;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import io.github.stack.guice.plugins.StackPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides a REST interface into managing {@link AbstractScheduledService}.
 * The manager class, {@link ScheduledServiceManager} can be injected with {@code Provider<ScheduledServiceManager>}.
 */
public final class ScheduledServicePlugin extends StackPlugin {

    private static final Logger log = LoggerFactory.getLogger(ScheduledServicePlugin.class);

    @Override
    protected void configure() {
        bind(ScheduledServiceManagerResource.class).in(Scopes.SINGLETON);
    }

    @Singleton
    @Provides
    public ScheduledServiceManager scheduledServiceManagerProvider(final Injector injector) {
        try {
            return createScheduledServiceManager(injector);
        } finally {
            startServices(injector);
            addShutdownHook(injector);
        }
    }

    private static ScheduledServiceManager createScheduledServiceManager(final Injector injector) {
        final ScheduledServiceManager scheduledServiceManager = new ScheduledServiceManager();
        getScheduledServices(injector).forEach(scheduledServiceManager::addService);
        return scheduledServiceManager;
    }

    private static void startServices(final Injector injector) {
        getScheduledServices(injector).forEach(scheduledService -> {
            log.info("Starting " + scheduledService.getClass());
            scheduledService.start();
        });
    }

    private static void addShutdownHook(final Injector injector) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                getScheduledServices(injector).forEach(scheduledService -> {
                    log.info("Stopping " + scheduledService.getClass());
                    scheduledService.stop();
                });
            }
        });
    }

    private static List<AbstractScheduledService> getScheduledServices(final Injector injector) {
        final List<AbstractScheduledService> scheduledServices = new LinkedList<>();
        final Map<Key<?>, Binding<?>> allBindings = injector.getAllBindings();
        for (final Map.Entry<Key<?>, Binding<?>> entry : allBindings.entrySet()) {
            final Key<?> key = entry.getKey();

            final Class<?> clazz = key.getTypeLiteral().getRawType();
            if (AbstractScheduledService.class.isAssignableFrom(clazz)) {
                scheduledServices.add(AbstractScheduledService.class.cast(entry.getValue().getProvider().get()));
            }
        }
        return scheduledServices;
    }
}