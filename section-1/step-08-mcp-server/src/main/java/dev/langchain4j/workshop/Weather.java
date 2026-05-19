package dev.langchain4j.workshop;

import io.openliberty.mcp.annotations.Tool;
import io.openliberty.mcp.annotations.ToolArg;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class Weather {
    @RestClient
    WeatherClient weatherClient;

    @Tool(
        name = "search_weather",
        title = "Search for weather.",
        description = "Get weather forecast for a location."
    )
    String getForecast(
        @ToolArg(
            name = "latitude",
            description = "Latitude of the location"
        ) double latitude,
        @ToolArg(
            name = "longitude",
            description = "Longitude of the location"
        ) double longitude
    ) {
        return weatherClient.getForecast(
            latitude,
            longitude,
            16,
            "temperature_2m,snowfall,rain,precipitation,precipitation_probability"
        );
    }
}