package io.github.mikexliu.stack.guice.plugins.app.scheduledservice;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import io.github.mikexliu.stack.guice.plugins.app.AppPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides a REST interface into managing ScheduledServices.
 */
public final class ScheduledServicePlugin extends AppPlugin {

    private static final Logger log = LoggerFactory.getLogger(ScheduledServicePlugin.class);

    @Override
    protected void configure() {
        bind(ScheduledServiceManagerResource.class).in(Scopes.SINGLETON);
    }

    @Singleton
    @Provides
    public ScheduledServiceManager servicesManagerProvider(final Injector injector) {
        try {
            return createServicesManager(injector);
        } finally {
            startServices(injector);
            addShutdownHook(injector);
        }
    }

    private static ScheduledServiceManager createServicesManager(final Injector injector) {
        final ScheduledServiceManager scheduledServiceManager = new ScheduledServiceManager();
        final List<AbstractScheduledService> scheduledServices = new LinkedList<>();
        final Map<Key<?>, Binding<?>> allBindings = injector.getAllBindings();

        for (final Map.Entry<Key<?>, Binding<?>> entry : allBindings.entrySet()) {
            final Key<?> key = entry.getKey();

            final Class<?> clazz = key.getTypeLiteral().getRawType();
            if (AbstractScheduledService.class.isAssignableFrom(clazz)) {
                scheduledServices.add(AbstractScheduledService.class.cast(entry.getValue().getProvider().get()));
            }
        }
        scheduledServices.forEach(scheduledServiceManager::addService);
        return scheduledServiceManager;
    }

    private static void startServices(final Injector injector) {
        getScheduledServices(injector).forEach(scheduledService -> {
            log.info("Starting " + scheduledService.getClass());
            scheduledService.startAsync();
        });
    }

    private static void addShutdownHook(final Injector injector) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                getScheduledServices(injector).forEach(scheduledService -> {
                    log.info("Stopping " + scheduledService.getClass());
                    scheduledService.stopAsync();
                });
            }
        });
    }

    private static List<com.google.common.util.concurrent.AbstractScheduledService> getScheduledServices(final Injector injector) {
        final List<com.google.common.util.concurrent.AbstractScheduledService> scheduledServices = new LinkedList<>();
        final Map<Key<?>, Binding<?>> allBindings = injector.getAllBindings();
        for (final Map.Entry<Key<?>, Binding<?>> entry : allBindings.entrySet()) {
            final Key<?> key = entry.getKey();

            final Class<?> clazz = key.getTypeLiteral().getRawType();
            if (com.google.common.util.concurrent.AbstractScheduledService.class.isAssignableFrom(clazz)) {
                scheduledServices.add(com.google.common.util.concurrent.AbstractScheduledService.class.cast(entry.getValue().getProvider().get()));
            }
        }
        return scheduledServices;
    }
}