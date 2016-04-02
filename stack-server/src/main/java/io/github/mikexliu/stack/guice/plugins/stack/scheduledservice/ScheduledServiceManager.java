package io.github.mikexliu.stack.guice.plugins.stack.scheduledservice;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ScheduledServiceManager {

    private static final Logger log = LoggerFactory.getLogger(ScheduledServiceManager.class);

    private final Map<String, AbstractScheduledService> services;

    public ScheduledServiceManager() {
        services = new HashMap<>();
    }

    public void addService(final AbstractScheduledService service) {
        String simpleName = service.getClass().getSimpleName();
        if (simpleName.contains("EnhancerByGuice")) {
            simpleName = service.getClass().getSuperclass().getSimpleName();
        }

        services.put(simpleName, service);
    }

    public void runOnce(final String name) {
        if (services.containsKey(name)) {
            final AbstractScheduledService service = services.get(name);
            if (service instanceof Runnable) {
                ((Runnable) service).run();
            }
        }
    }

    public void start(final String name) {
        if (services.containsKey(name)) {
            final AbstractScheduledService service = services.get(name);
            if (!service.isRunning()) {
                services.get(name).startAsync();
            }
        }
    }

    public void stop(final String name) {
        if (services.containsKey(name)) {
            final AbstractScheduledService service = services.get(name);
            if (service.isRunning()) {
                services.get(name).stopAsync();
            }
        }
    }

    public Map<String, Service.State> getServices() {
        final Map<String, Service.State> states = new HashMap<>();
        for (final Map.Entry<String, AbstractScheduledService> entry : services.entrySet()) {
            states.put(entry.getKey(), entry.getValue().state());
        }
        return states;
    }

    public void startAll() {
        getServices().keySet().forEach(this::start);
    }

    public void stopAll() {
        getServices().keySet().forEach(this::stop);
    }
}
