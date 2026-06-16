package dev.langchain4j.workshop;

import org.mcp_java.annotations.tools.Tool;
import org.mcp_java.annotations.tools.ToolArg;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MCP Tools for accessing Meteo API functionality.
 */
@ApplicationScoped
public class WeatherTools {

    private static final Logger logger = LoggerFactory.getLogger(WeatherTools.class);

    @Inject
    @RestClient
    private WeatherClient weatherClient;

    /**
     * Query the weather for the specified latitude and longitude
     * 
     * @param lattitude
     *          Latitude of the location
     * @param longitude
     *          Longitude of the location
     * @return String
     *          The response from the Meteo API
     */
    @Tool(
        name = "search_weather",
        title = "Search for weather.",
        description = "Search for weather at the specified location. "
    )
    public String searchNews(
        @ToolArg(
            name = "latitude",
            description = "Latitude of the location."
        )
        double latitude,
        @ToolArg(
            name = "longitude",
            description = "Longitude of the location"
        )
        double longitude
    ) {
        
        logger.info("Querying the weather for location {}, {}", latitude, longitude);
        
        try {
            return weatherClient.getForecast(
                latitude,
                longitude,
                16,
                "temperature_2m,snowfall,rain,precipitation,precipitation_probability"
            );
        } catch (Exception e) {
            logger.error("Error querying weather", e);
            throw new RuntimeException("Failed to query weather: " + e.getMessage(), e);
        }
    }
}
