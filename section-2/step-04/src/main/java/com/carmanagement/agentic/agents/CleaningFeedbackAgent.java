package com.carmanagement.agentic.agents;

import dev.langchain4j.cdi.spi.RegisterSimpleAgent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Agent that analyzes feedback to determine if a cleaning is needed.
 */
@RegisterSimpleAgent(
    name = "cleaning-feedback-agent",
    description = "Cleaning analyzer. Using feedback, determines if a cleaning is needed.",
    chatModelName = "chat-model",
    chatMemoryName = "cleaning-feedback-agent-memory",
    outputKey = "cleaningRequest",
    scope = ApplicationScoped.class
)
public interface CleaningFeedbackAgent {

    @SystemMessage("""
        You are a cleaning analyzer for a car rental company.
        Your job is to determine if a car needs cleaning based on feedback and past car condition.
        Maintenance never cleans cars.  If car needed cleaning prior to maintenance, the car will still need cleaning afterwards.
        If the feedback or car condition mentions dirt, mud, stains, or anything that suggests the car is dirty, recommend a cleaning.
        If there's cleaning feedback, prioritize it over the previous car condition.
        Be specific about what type of cleaning is needed (exterior, interior, detailing, waxing).
        If no interior or exterior car cleaning services are needed based on the feedback, respond with "CLEANING_NOT_REQUIRED".
        Include the reason for your choice but keep your response short.
    """)
    @UserMessage("""
        Car Information:
        Make: {{carMake}}
        Model: {{carModel}}
        Year: {{carYear}}
        Previous Condition: {{carCondition}}
        
        Feedback: {{feedback}}
    """)
    String analyzeForCleaning(
        @V("carMake") String carMake,
        @V("carModel") String carModel,
        @V("carYear") Integer carYear,
        @V("carNumber") Integer carNumber,
        @V("carCondition") String carCondition,
        @V("feedback") String feedback
    );
}
