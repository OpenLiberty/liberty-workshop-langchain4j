package com.carmanagement.agentic.workflows;

import com.carmanagement.models.CarConditions;

import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.cdi.spi.RegisterSequenceAgent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workflow for processing car returns using a sequence of agents.
 */
@RegisterSequenceAgent(
    name = "car-processing-workflow",
    subAgentNames = {
        "feedback-analysis-workflow",
        "fleet-supervisor-agent",
        "car-condition-feedback-agent"
    },
    outputKey = "carProcessingAgentResult"
)
public interface CarProcessingWorkflow {

    /**
     * Processes a car return by first analyzing feedback, then using supervisor to coordinate actions.
     * FeedbackAnalysisWorkflow analyzes feedback in parallel and returns FeedbackAnalysisResults via its @Output method.
     * FleetSupervisorAgent uses these results to coordinate action agents.
     * CarConditionFeedbackAgent determines the final car assignment and condition.
     */
    CarConditions processCarReturn(
        List<String> tasks,
        String carMake,
        String carModel,
        Integer carYear,
        Integer carNumber,
        String carCondition,
        String feedback
    );

    @Output
    static CarConditions output(CarConditions carConditions) {
        final Logger logger = LoggerFactory.getLogger(CarProcessingWorkflow.class);

        // CarConditionFeedbackAgent handles all logic for determining
        // the final car assignment and condition description.
        logger.debug("CarConditions: {} → {} ", carConditions.generalCondition(), carConditions.carAssignment());
        return carConditions;
    }
}