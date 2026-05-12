package dev.langchain4j.workshop;

import dev.langchain4j.cdi.spi.RegisterAIService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAIService(
    chatModelName = "customer-support-agent",
    scope = ApplicationScoped.class
)
public interface CustomerSupportAgent {

    String chat(String userMessage);
}
