package com.carmanagement.agentic.workflows;

import com.carmanagement.agentic.agents.CarConditionFeedbackAgent;
import com.carmanagement.agentic.agents.CleaningAgent;
import com.carmanagement.models.CarConditions;

import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.cdi.spi.RegisterSequenceAgent;

/**
 * Workflow for processing car returns using a sequence of agents.
 */
@RegisterSequenceAgent(
    name = "car-processing-workflow-agent",
    subAgentNames = {
        "cleaning-agent",
        "car-condition-feedback-agent"
    },
    outputKey = "carConditions"
)
public interface CarProcessingWorkflow {

    /**
     * Processes a car return by running feedback analysis and then appropriate actions.
     */
    CarConditions processCarReturn(
        String carMake,
        String carModel,
        Integer carYear,
        Integer carNumber,
        String carCondition,
        String feedback
    );

    @Output
    static CarConditions output(String carCondition, String cleaningAgentResult) {
        boolean cleaningRequired = !cleaningAgentResult.toUpperCase().contains("NOT_REQUIRED");
        return new CarConditions(carCondition, cleaningRequired);
    }
}