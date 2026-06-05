package com.carmanagement.agentic.workflows;

import com.carmanagement.agentic.agents.FeedbackAnalysisAgent;
import com.carmanagement.models.CarInfo;
import com.carmanagement.models.FeedbackAnalysisResults;
import com.carmanagement.models.FeedbackTask;

import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.agentic.declarative.ParallelMapperAgent;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.cdi.spi.RegisterParallelMapperAgent;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workflow for processing car feedback in parallel.
 * Analyzes feedback for cleaning, maintenance, and disposition needs using a unified agent.
 */
@RegisterParallelMapperAgent(
    name = "feedback-analysis-workflow",
    description = "Analyzes car feedback in parallel for cleaning, maintenance, and disposition needs",
    subAgentNames = {
        "feedback-analysis-agent"
    },
    itemsKey = "tasks",
    outputKey = "feedbackAnalysisResults"
)
public interface FeedbackAnalysisWorkflow {

    /**
     * Runs the feedback analysis agent in parallel for multiple tasks.
     * Uses @ParallelMapperAgent to execute the same agent with different task configurations.
     * Returns a list of results that will be mapped to individual output keys.
     */
    FeedbackAnalysisResults analyzeFeedback(
        List<String> tasks,
        String carMake,
        String carModel,
        Integer carYear,
        Integer carNumber,
        String carCondition,
        String feedback
    );

    /**
     * Output method that transforms the parallel feedback results into a structured object.
     * The feedbackAnalysisResults list contains results in the same order as the input tasks:
     * [0] = cleaning analysis, [1] = maintenance analysis, [2] = disposition analysis
     */
    @Output
    static FeedbackAnalysisResults output(
        AgenticScope scope,
        List<String> feedbackAnalysisResults
    ) {
        /*
         * We need to write the output from each run of the FeedbackAnalysisAgent to separate entries in the agentic
         * scope so that they can be referenced individually by downstream agents.
         */
        scope.writeState("cleaningAnalysis", feedbackAnalysisResults.get(0));
        scope.writeState("maintenanceAnalysis", feedbackAnalysisResults.get(1));
        scope.writeState("dispositionAnalysis", feedbackAnalysisResults.get(2));

        return new FeedbackAnalysisResults(
            feedbackAnalysisResults.get(0),  // cleaningAnalysis
            feedbackAnalysisResults.get(1),  // maintenanceAnalysis
            feedbackAnalysisResults.get(2)   // dispositionAnalysis
        );
    }
}