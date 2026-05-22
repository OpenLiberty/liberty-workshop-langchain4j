package com.carmanagement.agentic.workflows;

import com.carmanagement.agentic.agents.CleaningAgent;
import com.carmanagement.agentic.agents.MaintenanceAgent;

import dev.langchain4j.agentic.declarative.ActivationCondition;
import dev.langchain4j.cdi.spi.RegisterConditionalAgent;

/**
 * Workflow for assigning cars to appropriate teams based on feedback analysis.
 */
@RegisterConditionalAgent(
    name = "car-assignment-workflow",
    subAgentNames = {
        "maintenance-agent",
        "cleaning-agent"
    },
    outputKey = "analysisResult"
)
public interface CarAssignmentWorkflow {

    /**
     * Assigns the car to the appropriate team based on the feedback analysis.
     */
    String processAction(
        String carMake,
        String carModel,
        Integer carYear,
        Integer carNumber,
        String carCondition,
        String cleaningRequest,
        String maintenanceRequest
    );

    @ActivationCondition(MaintenanceAgent.class)
    static boolean assignToMaintenance(String maintenanceRequest) {
        return isRequired(maintenanceRequest);
    }

    @ActivationCondition(CleaningAgent.class)
    static boolean assignToCleaning(String maintenanceRequest, String cleaningRequest) {
        return !isRequired(maintenanceRequest) && isRequired(cleaningRequest);
    }

    private static boolean isRequired(String value) {
        return value != null && !value.isEmpty() && !value.toUpperCase().contains("NOT_REQUIRED");
    }
}
