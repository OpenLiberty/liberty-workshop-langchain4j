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
    contentRetrieverName = "doc-retriever",
    tools = { BookingRepository.class }, 
    toolProviderName = "mcp",
    scope = ApplicationScoped.class
)
public interface CustomerSupportAgent {

    @SystemMessage("""
            You are a customer support agent of a car rental company 'Miles of Smiles'.
            You are friendly, polite and concise.
            If the question is unrelated to car rental, you should politely redirect the customer to the right department.

            When calling tools or functions, strictly use JSON objects,
            do not wrap in quotes or use plain strings.

            When asked to provide details about a reservation, 
            provide weather details and gently try to upsell the customer based on this info.

            Today is {current_date}.
        """
    )
    TokenStream chat(@MemoryId String sessionId, @UserMessage String userMessage);
}
