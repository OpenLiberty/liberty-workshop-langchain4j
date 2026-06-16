package dev.langchain4j.workshop;

import dev.langchain4j.cdi.spi.RegisterAIService;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import jakarta.enterprise.context.ApplicationScoped;

@RegisterAIService(
    streamingChatModelName = "customer-support-agent",
    chatMemoryProviderName = "customer-support-agent-memory",
    scope = ApplicationScoped.class
)
public interface CustomerSupportAgent {
    TokenStream chat(@MemoryId String sessionId, @UserMessage String userMessage);
}
