package com.carmanagement.agentic.workflows;

import dev.langchain4j.cdi.spi.RegisterParallelAgent;

/**
 * Workflow for processing car feedback in parallel.
 */
// --8<-- [start:parallel-agent]
@RegisterParallelAgent(
    name = "feedback-workflow",
    subAgentNames = {
        "cleaning-feedback-agent",
        "maintenance-feedback-agent"
    },
    outputKey = "feedbackResult"
)
// --8<-- [end:parallel-agent]
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
