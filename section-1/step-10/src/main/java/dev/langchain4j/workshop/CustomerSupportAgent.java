package dev.langchain4j.workshop;

import dev.langchain4j.cdi.spi.RegisterAIService;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.workshop.Exceptions.BookingCannotBeCancelledException;
import dev.langchain4j.workshop.Exceptions.BookingNotFoundException;
import dev.langchain4j.workshop.Exceptions.CustomerNotFoundException;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.temporal.ChronoUnit;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

@RegisterAIService(
    streamingChatModelName = "customer-support-agent",
    chatMemoryProviderName = "customer-support-agent-memory",
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

            When asked to provide details about a reservation, 
            provide weather details and gently try to upsell the customer based on this info.

            Today is {current_date}.
        """
    )
    @Timeout(
        unit = ChronoUnit.SECONDS,
        value = 30
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
    String chat(@MemoryId String sessionId, @UserMessage String userMessage);

    default String chatFallback(String sessionId, String question) {
        return String.format(
            "Sorry, I am not able to answer your request \"%s\" at the moment. Please try again later.",
            question
        );
    }
}
