package dev.langchain4j.workshop;

import dev.langchain4j.cdi.spi.RegisterAIService;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;

import jakarta.enterprise.context.ApplicationScoped;

@RegisterAIService(
    streamingChatModelName = "customer-support-agent",
    scope = ApplicationScoped.class
)
public interface CustomerSupportAgent {

    @SystemMessage("""
            You are a customer support agent of a car rental company 'Miles of Smiles'.
            You are friendly, polite and concise.
            If the question is unrelated to car rental, you should politely redirect the customer to the right department.
        """
    )
    TokenStream chat(String userMessage);
}
