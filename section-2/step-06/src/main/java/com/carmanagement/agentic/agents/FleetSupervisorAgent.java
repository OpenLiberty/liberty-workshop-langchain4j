package com.carmanagement.agentic.agents;

import com.carmanagement.models.FeedbackAnalysisResults;

import dev.langchain4j.agentic.declarative.SupervisorRequest;
import dev.langchain4j.cdi.spi.RegisterSupervisorAgent;

/**
 * Supervisor agent that orchestrates the entire car processing workflow.
 * Coordinates feedback analysis agents and action agents based on car condition.
 * Implements human-in-the-loop pattern for high-value vehicle dispositions.
 */
@RegisterSupervisorAgent(
    name = "fleet-supervisor-agent",
    chatModelName = "chat-model",
    subAgentNames = {
        "pricing-agent",
        "disposition-proposal-agent",
        "human-approval-agent",
        "disposition-agent",
        "maintenance-agent",
        "cleaning-agent"
    },
    outputKey = "supervisorDecision"
)
public interface FleetSupervisorAgent {

    /**
     * Main method to coordinate car processing based on feedback.
     * This is the entry point for the supervisor agent.
     */
    String superviseCarProcessing(
        String carMake,
        String carModel,
        Integer carYear,
        Integer carNumber,
        String carCondition,
        String feedback,
        FeedbackAnalysisResults feedbackAnalysisResults
    );

    /**
     * Generates the supervisor request prompt based on feedback analysis results.
     * This method examines the disposition analysis to determine if the car requires
     * disposition (removal from fleet) and constructs appropriate instructions for
     * the supervisor agent to coordinate the necessary action agents.
     *
     * @param carMake The make of the car
     * @param carModel The model of the car
     * @param carYear The year of the car
     * @param carNumber The car's identification number
     * @param carCondition The current condition description
     * @param feedbackAnalysisResults The results from parallel feedback analysis
     * @return A formatted prompt instructing the supervisor which agents to invoke
     */
    @SupervisorRequest
    static String request(
        String carMake,
        String carModel,
        Integer carYear,
        Integer carNumber,
        String carCondition,
        String feedback,
        FeedbackAnalysisResults feedbackAnalysisResults
    ) {
        boolean dispositionRequired = feedbackAnalysisResults.dispositionAnalysis() != null &&
            feedbackAnalysisResults.dispositionAnalysis().toUpperCase().contains("DISPOSITION_REQUIRED");

        String noDispositionMessage = """
                Disposition is not required. 
                Proceed with normal maintenance and cleaning workflow. 
                If cleaning or maintenance is required, invoke the appropriate agents.
            """;

        // Disposition required - complex path
        String dispositionMessage = """
                DISPOSITION_REQUIRED
                
                Follow these steps:
                
                1. Get value from PricingAgent (keep $ format)
                2. IF value > $15,000 (HIGH-VALUE):
                    - Invoke DispositionProposalAgent → HumanApprovalAgent (workflow pauses)
                    - APPROVED: Use AI recommendation → KEEP→"KEEP_CAR", DISPOSE→"DISPOSE_CAR"
                    - REJECTED: Opposite of AI → KEEP→"DISPOSE_CAR", DISPOSE→"KEEP_CAR"
                3. IF value ≤ $15,000 (LOW-VALUE):
                    - Invoke DispositionAgent directly
                    - KEEP→"KEEP_CAR", SCRAP/SELL/DONATE→"DISPOSE_CAR"
                4. IF "KEEP_CAR": Invoke MaintenanceAgent/CleaningAgent as needed
                
                CRITICAL: End with KEEP_CAR or DISPOSE_CAR
            """;

        return String.format("""
            You are a fleet supervisor for a car rental company. You coordinate action agents based on feedback analysis.
            
            The feedback has already been analyzed and you have these inputs:
            - cleaningAnalysis: What cleaning is needed (or "CLEANING_NOT_REQUIRED")
            - maintenanceAnalysis: What maintenance is needed (or "MAINTENANCE_NOT_REQUIRED")
            - dispositionAnalysis: Whether severe damage requires disposition (or "DISPOSITION_NOT_REQUIRED")
            
            Your job is to invoke the appropriate ACTION agents for this car
            
            Car: %d %s %s (#%d)
            Current Condition: %s
            Feedback: %s
            
            Cleaning Analysis: %s
            Maintenance Analysis: %s
            Disposition Analysis: %s
            """,
            carYear,
            carMake,
            carModel,
            carNumber,
            carCondition,
            feedback,
            feedbackAnalysisResults.cleaningAnalysis(),
            feedbackAnalysisResults.maintenanceAnalysis(),
            dispositionRequired ? dispositionMessage : noDispositionMessage
        );
    }
}