package io.github.mikexliu.stack.guice.plugins.stack.scheduledservice;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import io.github.mikexliu.stack.guice.plugins.stack.StackPlugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Binds ServiceResource into swagger.
 * Finds all AbstractScheduledServices and starts them.
 */
public class ScheduledServiceManagerPlugin extends StackPlugin {

    private final ScheduledServiceManager scheduledServiceManager;

    public ScheduledServiceManagerPlugin(final Injector injector) {
        scheduledServiceManager = createServicesManager(injector);

        //addShutdownHook(scheduledServiceManager);
    }

    @Override
    protected void configure() {
        bind(ScheduledServiceManagerResource.class).in(Scopes.SINGLETON);
    }

    @Singleton
    @Provides
    public ScheduledServiceManager servicesManagerProvider() {
        return scheduledServiceManager;
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

//    private static void addShutdownHook(final ScheduledServiceManager scheduledServiceManager) {
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            @Override
//            public void run() {
//                scheduledServiceManager.stopAll();
//            }
//        });
//    }
}
