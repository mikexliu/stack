package io.github.mikexliu.stack.guice.modules.scheduledservice;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Binds ServiceResource into swagger.
 * Finds all AbstractScheduledServices and starts them.
 */
public class ServicesManagerModule extends AbstractModule {

    private final ServicesManager servicesManager;

    public ServicesManagerModule(final Injector injector) {
        servicesManager = createServicesManager(injector);

        addShutdownHook(servicesManager);
    }

    @Override
    protected void configure() {
        bind(ServiceManagerResource.class).in(Scopes.SINGLETON);
    }

    @Singleton
    @Provides
    public ServicesManager servicesManagerProvider() {
        return servicesManager;
    }

    private static ServicesManager createServicesManager(final Injector injector) {
        final ServicesManager servicesManager = new ServicesManager();
        final List<AbstractScheduledService> scheduledServices = new LinkedList<>();
        final Map<Key<?>, Binding<?>> allBindings = injector.getAllBindings();
        for (final Map.Entry<Key<?>, Binding<?>> entry : allBindings.entrySet()) {
            final Key<?> key = entry.getKey();

            final Class<?> clazz = key.getTypeLiteral().getRawType();
            if (AbstractScheduledService.class.isAssignableFrom(clazz)) {
                scheduledServices.add(AbstractScheduledService.class.cast(entry.getValue().getProvider().get()));
            }
        }
        scheduledServices.forEach(servicesManager::addService);
        return servicesManager;
    }

    private static void addShutdownHook(final ServicesManager servicesManager) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                servicesManager.stopAll();
            }
        });
    }
}
