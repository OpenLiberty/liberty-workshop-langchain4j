package dev.langchain4j.workshop;

import dev.langchain4j.cdi.spi.RegisterAIService;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import jakarta.enterprise.context.ApplicationScoped;

@RegisterAIService(
    streamingChatModelName = "customer-support-agent",
    chatMemoryProviderName = "customer-support-agent-memory",
    scope = ApplicationScoped.class
)
public interface CustomerSupportAgent {
    @SystemMessage("""
            You are a customer support agent of a car rental company 'Miles of Smiles'.
            You are friendly, polite and concise.
            If the question is unrelated to car rental, you should politely redirect the customer to the right department.
        """
    )
    TokenStream chat(@MemoryId String sessionId, @UserMessage String userMessage);
}
