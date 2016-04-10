package io.github.mikexliu.stack.guice.plugins.stack.metrics;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;

/**
 * TODO: how to send this to app interceptors or managers?
 */
public class MetricsManager {

    private final MetricRegistry metricRegistry;

    @Inject
    public MetricsManager(final MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }



    @Override
    public String toString() {
        return metricRegistry.toString();
    }
}
