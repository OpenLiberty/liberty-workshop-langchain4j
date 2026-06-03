package com.carmanagement.resources;

import java.util.List;

import com.carmanagement.exceptions.CarInfoNotFoundException;
import com.carmanagement.managers.CarInfoManager;
import com.carmanagement.models.CarInfo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

/**
 * REST resource for car operations.
 */
@ApplicationScoped
@Path("/cars")
@Produces("application/json")
public class CarResource {

    @Inject
    private CarInfoManager carInfoManager;

    /**
     * Get all cars in the system.
     * 
     * @return List of all cars
     */
    @GET
    public List<CarInfo> getAllCars() {
        return carInfoManager.getCars(0, 100);
    }
    
    /**
     * Get a specific car by its ID.
     * 
     * @param id The car ID
     * @return The car with the specified ID, or 404 if not found
     */
    @GET
    @Path("/{id}")
    public Response getCarById(Integer id) {
        try {
            CarInfo car = carInfoManager.getCar(id);
            return Response.ok(car).build();
        } catch (CarInfoNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("Car with ID " + id + " not found")
                .build();
        }
    }
}
