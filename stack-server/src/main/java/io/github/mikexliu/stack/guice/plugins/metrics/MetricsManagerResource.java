package io.github.mikexliu.stack.guice.plugins.metrics;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Api(value = "Metrics manager api", description = "Metrics description")
@Path("/api/stack/metrics/v1")
public final class MetricsManagerResource {

    private static final Logger log = LoggerFactory.getLogger(MetricsManagerResource.class);

    private final MetricsManager metricsManager;

    @Inject
    public MetricsManagerResource(final MetricsManager metricsManager) {
        this.metricsManager = metricsManager;
    }

    @ApiOperation(value = "get-metrics",
            notes = "Returns all available metrics")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/get-metrics")
    public Map<String, Map<String, String>> getServices() {
        return metricsManager.getMetrics();
    }
}
