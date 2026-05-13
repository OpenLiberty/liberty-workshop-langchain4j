package dev.langchain4j.workshop;

import dev.langchain4j.cdi.spi.RegisterAIService;
import dev.langchain4j.service.TokenStream;

import jakarta.enterprise.context.ApplicationScoped;

@RegisterAIService(
    streamingChatModelName = "customer-support-agent",
    scope = ApplicationScoped.class
)
public interface CustomerSupportAgent {

    TokenStream chat(String userMessage);
}
