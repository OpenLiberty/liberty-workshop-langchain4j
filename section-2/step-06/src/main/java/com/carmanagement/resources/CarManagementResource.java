package com.carmanagement.resources;

import com.carmanagement.services.CarManagementService;

import dev.langchain4j.data.message.ImageContent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.slf4j.Logger;

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
     * Process a car return from any status (rental, cleaning, or maintenance).
     * This is a blocking operation due to AI agent processing.
     *
     * @param carNumber The car number
     * @param feedback Optional feedback about the return
     * @param carImage Optional image of the car being returned (multipart form data)
     * @return Result of the processing
     */
    @POST
    @Path("/return/{carNumber}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response processReturn(
        @PathParam("carNumber") Integer carNumber,
        @FormParam("feedback") String feedback,
        @FormParam("carImage") EntityPart carImage
    ) {
        logger.info("Processing rental return for car {} with feedback: {}", carNumber, feedback);

        ImageContent imageContent = toImageContent(carImage);

        try {
            String result = carManagementService.processCarReturn(carNumber, feedback != null ? feedback : "", imageContent);
            return Response.ok(result).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error processing return: " + e.getMessage())
                .build();
        }
    }

    @GET
    @Path("/report")
    @Produces(MediaType.TEXT_HTML)
    public Response report() {
        return Response.ok(carManagementService.report()).build();
    }

    private ImageContent toImageContent(EntityPart carImage) {
        if (carImage == null) {
            logger.info("No image provided");
            return null;
        }
        try {
            logger.info("Image provided: {}", carImage.getFileName().orElse(""));

            InputStream is = carImage.getContent();
            MediaType mediaType = carImage.getMediaType();
            byte[] bytes = is.readAllBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            return new ImageContent(base64, mediaType.toString());
        } catch (IOException e) {
            logger.error("Failed to read uploaded car image", e);
            return null;
        }
    }
}