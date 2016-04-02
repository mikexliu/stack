package io.github.mikexliu.stack.guice.plugins.stack.scheduledservice;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ServicesManager {

    private static final Logger log = LoggerFactory.getLogger(ServicesManager.class);

    private final Map<String, AbstractScheduledService> services;

    public ServicesManager() {
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
                log.info("Running " + name + " once");
                ((Runnable) service).run();
            }
        }
    }

    public void start(final String name) {
        if (services.containsKey(name)) {
            final AbstractScheduledService service = services.get(name);
            if (!service.isRunning()) {
                log.info("Starting " + name);
                services.get(name).startAsync();
            }
        }
    }

    public void stop(final String name) {
        if (services.containsKey(name)) {
            final AbstractScheduledService service = services.get(name);
            if (service.isRunning()) {
                log.info("Stopping " + name);
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
        log.info("Starting services");
        getServices().keySet().forEach(scheduledService -> start(scheduledService));
        log.info("All services started");
    }

    public void stopAll() {
        log.info("Stopping services");
        getServices().keySet().forEach(scheduledService -> stop(scheduledService));
        log.info("All services stopped");
    }
}
