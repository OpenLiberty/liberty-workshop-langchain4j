package com.carmanagement.agentic.agents;

import dev.langchain4j.cdi.spi.RegisterSimpleAgent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Agent that analyzes feedback to update the car condition.
 */
@RegisterSimpleAgent(
    name = "car-condition-feedback-agent",
    description = "Car condition analyzer. Determines the current condition of a car based on feedback.",
    chatModelName = "chat-model",
    chatMemoryName = "car-condition-feedback-agent-memory",
    outputKey = "carCondition",
    scope = ApplicationScoped.class
)
public interface CarConditionFeedbackAgent {

    @SystemMessage("""
        You are a car condition analyzer for a car rental company. Your job is to determine the current condition of a car based on feedback.
        Analyze all feedback and the previous car condition to provide an updated condition description.
        Always provide a very short (no more than 200 characters) condition description, even if there's minimal feedback.
        Do not add any headers or prefixes to your response.
    """)
    @UserMessage("""
        Car Information:
        Make: {{carMake}}
        Model: {{carModel}}
        Year: {{carYear}}
        Previous Condition: {{carCondition}}

        Feedback from other agents:
        Cleaning Recommendation: {{cleaningRequest}}
        Maintenance Recommendation: {{maintenanceRequest}}
    """)
    String analyzeForCondition(
        @V("carMake") String carMake,
        @V("carModel") String carModel,
        @V("carYear") Integer carYear,
        @V("carNumber") Integer carNumber,
        @V("carCondition") String carCondition,
        @V("cleaningRequest") String cleaningRequest,
        @V("maintenanceRequest") String maintenanceRequest
    );
}
