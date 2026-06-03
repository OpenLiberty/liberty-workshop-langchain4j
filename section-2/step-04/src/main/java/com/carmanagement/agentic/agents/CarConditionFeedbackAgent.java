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
    description = "Final car condition analyzer. Determines the car's condition and assignment based on all feedback.",
    chatModelName = "chat-model",
    chatMemoryName = "car-condition-feedback-agent-memory",
    outputKey = "carConditions",
    scope = ApplicationScoped.class
)
public interface CarConditionFeedbackAgent {

    @SystemMessage("""
        Analyze car processing results and output a JSON summary.
        
        Output format:
        {
          "generalCondition": "concise description (max 200 chars)",
          "carAssignment": "DISPOSITION|MAINTENANCE|CLEANING|NONE"
        }
        
        Rules:
        - carAssignment: Check the ACTUAL DispositionAgent decision in supervisorDecision, not just the analysis
        - If supervisorDecision mentions SCRAP/SELL/DONATE (but NOT KEEP) → DISPOSITION
        - Else if maintenanceAnalysis ≠ "MAINTENANCE_NOT_REQUIRED" → MAINTENANCE
        - Else if cleaningAnalysis ≠ "CLEANING_NOT_REQUIRED" → CLEANING
        - Else → NONE
        - IMPORTANT: If DispositionAgent decided KEEP, do NOT assign DISPOSITION - check maintenance/cleaning instead
        - generalCondition: Summarize the action and reason
    """)
    @UserMessage("""
        Car: {{carYear}} {{carMake}} {{carModel}} (#{{carNumber}})
        
        Supervisor Decision: {{supervisorDecision}}
        
        - Disposition: {{dispositionAnalysis}}
        - Maintenance: {{maintenanceAnalysis}}
        - Cleaning: {{cleaningAnalysis}}

    """)
    String analyzeForCondition(
        @V("carMake") String carMake,
        @V("carModel") String carModel,
        @V("carYear") Integer carYear,
        @V("carNumber") Integer carNumber,
        @V("carCondition") String carCondition,
        @V("cleaningAnalysis") String cleaningAnalysis,
        @V("maintenanceAnalysis") String maintenanceAnalysis,
        @V("dispositionAnalysis") String dispositionAnalysis,
        @V("supervisorDecision") String supervisorDecision
    );
}
