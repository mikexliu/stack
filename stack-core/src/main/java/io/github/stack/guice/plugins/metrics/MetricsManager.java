package io.github.stack.guice.plugins.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MetricsManager {

    private static final Logger log = LoggerFactory.getLogger(MetricsManager.class);

    private final MetricRegistry metricRegistry;

    @Inject
    public MetricsManager(final MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public Map<String, Map<String, String>> getMetrics() {
        final Map<String, Map<String, String>> metrics = new HashMap<>();

        metricRegistry.getTimers().entrySet().forEach(entry -> {
            final Map<String, String> data = new HashMap<>();
            final Timer timerMetric = entry.getValue();
            final Snapshot snapshot = entry.getValue().getSnapshot();
            data.put("count", Long.toString(timerMetric.getCount()));
            data.put("min", Long.toString(snapshot.getMin()));
            data.put("max", Long.toString(snapshot.getMax()));
            data.put("mean", Double.toString(snapshot.getMean()));

            metrics.put(entry.getKey(), data);
        });
        return metrics;
    }
}
