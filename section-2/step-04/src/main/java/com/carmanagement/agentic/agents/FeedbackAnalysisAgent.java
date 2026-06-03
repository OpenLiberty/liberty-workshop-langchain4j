package com.carmanagement.agentic.agents;

import dev.langchain4j.cdi.spi.RegisterSimpleAgent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Unified agent that analyzes feedback based on the provided task configuration.
 * This agent is parameterized to handle cleaning, maintenance, and disposition analysis.
 */
@RegisterSimpleAgent(
    name = "feedback-analysis-agent",
    description = "Feedback analyzer. Using feedback, determines if action is needed based on task type.",
    chatModelName = "chat-model",
    outputKey = "feedbackAnalysis",
    scope = ApplicationScoped.class
)
public interface FeedbackAnalysisAgent {

    @SystemMessage("{{taskSystemInstructions}}")
    @UserMessage("""
        Car Information:
        Make: {{carMake}}
        Model: {{carModel}}
        Year: {{carYear}}
        Previous Condition: {{carCondition}}
        
        Feedback: {{feedback}}
    """)
    String analyzeFeedback(
        @V("taskSystemInstructions") String taskSystemInstructions,
        @V("carMake") String carMake,
        @V("carModel") String carModel,
        @V("carYear") Integer carYear,
        @V("carNumber") Integer carNumber,
        @V("carCondition") String carCondition,
        @V("feedback") String feedback
    );
}