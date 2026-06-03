package com.carmanagement.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carmanagement.services.CarManagementService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

/**
 * REST resource for car management operations.
 */
@ApplicationScoped
@Path("/car-management")
@Produces("application/json")
public class CarManagementResource {
    @Inject
    private Logger logger;

    @Inject
    CarManagementService carManagementService;

    /**
     * Process a car return.
     *
     * @param carNumber The car number
     * @param feedback Optional feedback
     * @return Result of the processing
     */
    @POST
    @Path("/return/{carNumber}")
    public Response processReturn(
        @PathParam("carNumber") Integer carNumber,
        @QueryParam("feedback") String feedback
    ) {

        try {
            String result = carManagementService.processCarReturn(carNumber, feedback != null ? feedback : "");
            return Response.ok(result).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error processing return: " + e.getMessage())
                .build();
        }
    }
}
