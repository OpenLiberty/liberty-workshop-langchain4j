package dev.langchain4j.workshop;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/")
public class WeatherService {
    @Inject
    @RestClient
    WeatherClient weatherClient;

    @Path("weather")
    @GET
    public String getWeather(
        @QueryParam("latitude") double latitude,
        @QueryParam("longitude") double longitude
    ){
        return weatherClient.getForecast(
            latitude,
            longitude,
            16,
            "temperature_2m,snowfall,rain,precipitation,precipitation_probability");
    }
}
