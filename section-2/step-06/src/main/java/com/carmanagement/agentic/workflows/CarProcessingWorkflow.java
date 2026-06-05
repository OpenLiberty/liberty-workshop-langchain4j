package com.carmanagement.agentic.workflows;

import com.carmanagement.models.CarConditions;

import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.agentic.observability.MonitoredAgent;
import dev.langchain4j.cdi.spi.RegisterSequenceAgent;
import dev.langchain4j.data.message.ImageContent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workflow for processing car returns using a sequence of agents.
 */
@RegisterSequenceAgent(
    name = "car-processing-workflow",
    subAgentNames = {
        "car-image-analysis-agent",
        "feedback-analysis-workflow",
        "fleet-supervisor-agent",
        "car-condition-feedback-agent"
    },
    outputKey = "carProcessingAgentResult"
)
public interface CarProcessingWorkflow extends MonitoredAgent {

    /**
     * Processes a car return by first analyzing feedback, then using supervisor to coordinate actions.
     * 
     * CarImageAnalysisAgent analyzes the car image first.
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
        String feedback,
        ImageContent carImage
    );

    @Output
    static CarConditions output(CarConditions carConditions) {
        final Logger logger = LoggerFactory.getLogger(CarProcessingWorkflow.class);

        // CarConditionFeedbackAgent now handles all the logic for determining
        // the final car assignment, disposition status, and condition description.
        // We simply pass through its result.
  
        logger.debug("DEBUG CarConditions output method:");
        logger.debug("  generalCondition: {}", carConditions.generalCondition());
        logger.debug("  carAssignment: {}", carConditions.carAssignment());
        logger.debug("  dispositionStatus: {}", carConditions.dispositionStatus());
        logger.debug("  dispositionReason: {}", carConditions.dispositionReason());

        return carConditions;
    }
}