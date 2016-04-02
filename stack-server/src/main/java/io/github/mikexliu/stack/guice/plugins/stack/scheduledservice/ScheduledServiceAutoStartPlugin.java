package io.github.mikexliu.stack.guice.plugins.stack.scheduledservice;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import io.github.mikexliu.stack.guice.plugins.stack.StackPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Auto starts and registers a shutdown hook to all SchceduledServices.
 */
public final class ScheduledServiceAutoStartPlugin extends StackPlugin {

    private static final Logger log = LoggerFactory.getLogger(ScheduledServiceAutoStartPlugin.class);

    private final Injector injector;

    public ScheduledServiceAutoStartPlugin(final Injector injector) {
        this.injector = injector;
    }

    @Override
    protected void configure() {
        startServices(injector);
        addShutdownHook(injector);
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

    private static void startServices(final Injector injector) {
        getScheduledServices(injector).forEach(scheduledService -> {
            log.info("Starting " + scheduledService);
            scheduledService.startAsync();
        });
    }

    private static void addShutdownHook(final Injector injector) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                getScheduledServices(injector).forEach(scheduledService -> {
                    log.info("Stopping " + scheduledService);
                    scheduledService.stopAsync();
                });
            }
        });
    }
}
