package com.carmanagement.agentic.agents;

import dev.langchain4j.cdi.spi.RegisterSimpleAgent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import jakarta.enterprise.context.ApplicationScoped;

// --8<-- [start:cleaningAgent]
/**
 * Agent that determines what cleaning services to request.
 */
@RegisterSimpleAgent(
    name = "cleaning-agent",
    description = "Cleaning specialist. Determines what cleaning services are needed.",
    chatModelName = "chat-model",
    chatMemoryName = "cleaning-agent-memory",
    toolNames = { "cleaning-tool" }, 
    scope = ApplicationScoped.class
)
public interface CleaningAgent {

    @SystemMessage("""
        You handle intake for the cleaning department of a car rental company.
        It is your job to submit a request to the provided requestCleaning function to take action based on the provided feedback.
        Be specific about what services are needed.
        If no cleaning is needed based on the feedback, respond with "CLEANING_NOT_REQUIRED".
    """)
    @UserMessage("""
        Car Information:
        Make: {{carMake}}
        Model: {{carModel}}
        Year: {{carYear}}
        Car Number: {{carNumber}}
        
        Feedback: {{feedback}}
    """)
    String processCleaning(
        @V("carMake") String carMake,
        @V("carModel") String carModel,
        @V("carYear") Integer carYear,
        @V("carNumber") Integer carNumber,
        @V("feedback") String feedback
    );
}
// --8<-- [end:cleaningAgent]