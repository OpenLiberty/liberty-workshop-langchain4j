package dev.langchain4j.workshop;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application class to activate REST endpoints.
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
    // No additional configuration needed - all @Path annotated classes will be discovered
}
