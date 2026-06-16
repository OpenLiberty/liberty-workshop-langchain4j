package dev.langchain4j.workshop;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri = "https://api.open-meteo.com/v1/forecast")
public interface WeatherClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    String getForecast(
        @QueryParam("latitude") double latitude,
        @QueryParam("longitude") double longitude,
        @QueryParam("forecastDays") int forecastDays,
        @QueryParam("hourly") String hourly
    );
}