package com.carmanagement.agentic.workflows;

import com.carmanagement.agentic.agents.CleaningFeedbackAgent;
import com.carmanagement.agentic.agents.MaintenanceFeedbackAgent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.cdi.spi.RegisterParallelAgent;

/**
 * Workflow for processing car feedback in parallel.
 */
@RegisterParallelAgent(
    name = "feedback-workflow",
    subAgentNames = {
        "cleaning-feedback-agent",
        "maintenance-feedback-agent"
    },
    outputKey = "feedbackResult"
)
public interface FeedbackWorkflow {

    /**
     * Runs multiple feedback agents in parallel to analyze different aspects of car feedback.
     */
    String analyzeFeedback(
        String carMake,
        String carModel,
        Integer carYear,
        Integer carNumber,
        String carCondition,
        String feedback
    );
}
