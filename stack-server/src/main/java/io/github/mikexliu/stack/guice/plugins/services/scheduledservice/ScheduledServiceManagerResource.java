package io.github.mikexliu.stack.guice.plugins.services.scheduledservice;

import com.google.common.util.concurrent.*;
import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Optional;

@Api(value = "Scheduled Services manager api", description = "Complete control Scheduled Services")
@Path("/api/stack/scheduled-services/v1")
public final class ScheduledServiceManagerResource {

    private static final Logger log = LoggerFactory.getLogger(ScheduledServiceManagerResource.class);

    private final ScheduledServiceManager scheduledServiceManager;

    @Inject
    public ScheduledServiceManagerResource(final ScheduledServiceManager scheduledServiceManager) {
        this.scheduledServiceManager = scheduledServiceManager;
    }

    @ApiOperation(value = "get-service-states",
            notes = "Returns all available services and its current state")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/get-service-states")
    public Map<String, Service.State> getServiceStates() {
        return scheduledServiceManager.getServiceStates();
    }

    @ApiOperation(value = "get-failure-cause",
            notes = "Returns the cause of the failure")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/get-failure-cause")
    public Throwable getFailureCause(
            @ApiParam(value = "service", required = true)
            @QueryParam(value = "service")
            final String service) {
        final Optional<com.google.common.util.concurrent.AbstractScheduledService> optionalService = scheduledServiceManager.getService(service);
        if (optionalService.isPresent()) {
            final com.google.common.util.concurrent.AbstractScheduledService scheduledService = optionalService.get();
            if (scheduledService.state() == Service.State.FAILED) {
                return scheduledService.failureCause();
            }
        }
        return null;
    }

    @POST
    @Path("/run-once")
    public void runOnce(
            @ApiParam(value = "service", required = true)
            @QueryParam(value = "service")
            final String service) {
        scheduledServiceManager.runOnce(service);
    }

    @POST
    @Path("/start")
    public void start(
            @ApiParam(value = "service", required = true)
            @QueryParam(value = "service")
            final String service) {
        scheduledServiceManager.start(service);
    }


    @POST
    @Path("/stop")
    public void stop(
            @ApiParam(value = "service", required = true)
            @QueryParam(value = "service")
            final String service) {
        scheduledServiceManager.stop(service);
    }
}
