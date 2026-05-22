package com.carmanagement.agentic.workflows;

import com.carmanagement.agentic.agents.CarConditionFeedbackAgent;
import com.carmanagement.agentic.agents.CleaningAgent;
import com.carmanagement.models.CarAssignment;
import com.carmanagement.models.CarConditions;

import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.cdi.spi.RegisterSequenceAgent;

/**
 * Workflow for processing car returns using a sequence of agents.
 */
@RegisterSequenceAgent(
    name = "car-processing-workflow",
    subAgentNames = {
        "feedback-workflow",
        "car-assignment-workflow",
        "car-condition-feedback-agent"
    },
    outputKey = "carProcessingAgentResult"
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
    static CarConditions output(String carCondition, String maintenanceRequest, String cleaningRequest) {
        CarAssignment carAssignment = CarAssignment.NONE;

        // Check maintenance first (higher priority)
        if (isRequired(maintenanceRequest)) {
            carAssignment = CarAssignment.MAINTENANCE;
        } else if (isRequired(cleaningRequest)) {
            carAssignment = CarAssignment.CLEANING;
        }

        return new CarConditions(carCondition, carAssignment);
    }

    private static boolean isRequired(String value) {
        return value != null && !value.isEmpty() && !value.toUpperCase().contains("NOT_REQUIRED");
    }
}