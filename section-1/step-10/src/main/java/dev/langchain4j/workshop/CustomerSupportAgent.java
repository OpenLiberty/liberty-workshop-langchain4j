package dev.langchain4j.workshop;

import dev.langchain4j.workshop.Exceptions.*;

import dev.langchain4j.cdi.spi.RegisterAIService;
import dev.langchain4j.service.SystemMessage;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.temporal.ChronoUnit;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

@RegisterAIService(
    streamingChatModelName = "customer-support-agent",
    chatMemoryName = "customer-support-agent-memory",
    contentRetrieverName = "doc-retriever",
    inputGuardrails = { PromptInjectionGuard.class },
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

            Today is {current_date}.
        """
    )
    @Timeout(
        unit = ChronoUnit.SECONDS,
        value = 1
    )
    @Retry(
        abortOn = {
            BookingCannotBeCancelledException.class,
            CustomerNotFoundException.class,
            BookingNotFoundException.class
        },
        maxRetries = 2,
        delay = 100
    )
    @Fallback(
        fallbackMethod = "chatFallback",
        skipOn = {
            BookingCannotBeCancelledException.class,
            CustomerNotFoundException.class,
            BookingNotFoundException.class
        }
    )
    String chat(String userMessage);

    default String chatFallback(String question) {
        return String.format(
            "Sorry, I am not able to answer your request \"%s\" at the moment. Please try again later.",
            question
        );
    }
}
