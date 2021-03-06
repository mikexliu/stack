package io.github.stack.guice.plugins.services.scheduledservice;

import com.google.common.util.concurrent.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ScheduledServiceManager {

    private static final Logger log = LoggerFactory.getLogger(ScheduledServiceManager.class);

    private final Map<String, AbstractScheduledService> services;

    /**
     * Package private constructor to prevent creation of this object.
     */
    ScheduledServiceManager() {
        services = new HashMap<>();
    }

    public void addService(final AbstractScheduledService service) {
        String simpleName = service.getClass().getSimpleName();
        if (simpleName.contains("EnhancerByGuice")) {
            simpleName = service.getClass().getSuperclass().getSimpleName();
        }

        services.put(simpleName, service);
    }

    public Optional<AbstractScheduledService> getService(final String name) {
        return Optional.ofNullable(services.get(name));
    }

    public void runOnce(final String name) {
        getService(name).ifPresent(service -> service.runOneIteration());
    }

    public void start(final String name) {
        getService(name).ifPresent(service -> {
            if (!service.isRunning()) {
                services.get(name).start();
            }
        });
    }

    public void stop(final String name) {
        getService(name).ifPresent(service -> {
            if (service.isRunning()) {
                services.get(name).stop();
            }
        });
    }

    public Map<String, Service.State> getServiceStates() {
        final Map<String, Service.State> states = new HashMap<>();
        for (final Map.Entry<String, AbstractScheduledService> entry : services.entrySet()) {
            states.put(entry.getKey(), entry.getValue().state());
        }
        return states;
    }

    public void startAll() {
        services.keySet().forEach(this::start);
    }

    public void stopAll() {
        services.keySet().forEach(this::stop);
    }
}
