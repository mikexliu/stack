package io.github.mikexliu.stack.guice.plugins.back.scheduledservice;

import com.google.common.util.concurrent.Service;
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

/**
 * Created by mliu on 3/27/16.
 */
@Api(value = "Scheduled Services manager api", description = "Complete control Scheduled Services")
@Path("/api/stack/scheduled-services/v1")
public final class ServiceManagerResource {

    private static final Logger log = LoggerFactory.getLogger(ServiceManagerResource.class);

    private final ServicesManager servicesManager;

    @Inject
    public ServiceManagerResource(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @ApiOperation(value = "get-services",
            notes = "Returns all available services and its current state")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/get-services")
    public Map<String, Service.State> getServices() {
        return servicesManager.getServices();
    }

    @POST
    @Path("/run-once")
    public void runOnce(
            @ApiParam(value = "service", required = true)
            @QueryParam(value = "service")
            final String service) {
        servicesManager.runOnce(service);
    }

    @POST
    @Path("/start")
    public void start(
            @ApiParam(value = "service", required = true)
            @QueryParam(value = "service")
            final String service) {
        servicesManager.start(service);
    }


    @POST
    @Path("/stop")
    public void stop(
            @ApiParam(value = "service", required = true)
            @QueryParam(value = "service")
            final String service) {
        servicesManager.stop(service);
    }
}
