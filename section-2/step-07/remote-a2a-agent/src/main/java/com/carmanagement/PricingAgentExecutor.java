package com.carmanagement;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.util.ArrayList;
import java.util.List;

import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.agentexecution.RequestContext;
import org.a2aproject.sdk.server.tasks.AgentEmitter;
import org.a2aproject.sdk.spec.A2AError;
import org.a2aproject.sdk.spec.Message;
import org.a2aproject.sdk.spec.Part;
import org.a2aproject.sdk.spec.Task;
import org.a2aproject.sdk.spec.TaskNotCancelableError;
import org.a2aproject.sdk.spec.TaskState;
import org.a2aproject.sdk.spec.TextPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for the PricingAgent.
 * Handles the integration between the A2A framework and the PricingAgent.
 */
@ApplicationScoped
public class PricingAgentExecutor {
    private static final Logger logger = LoggerFactory.getLogger(PricingAgentExecutor.class);

    @Produces
    public AgentExecutor agentExecutor(PricingAgent pricingAgent) {
        return new AgentExecutor() {
            @Override
            public void execute(RequestContext context, AgentEmitter agentEmitter) throws A2AError {
                logger.info("Remote A2A PricingAgent called");

                // Mark the task as submitted and start working on it
                if (context.getTask() == null) {
                    agentEmitter.submit();
                }
                agentEmitter.startWork();

                List<String> inputs = new ArrayList<>();

                // Process the request message
                Message message = context.getMessage();
                if (message.parts() != null) {
                    for (Part<?> part : message.parts()) {
                        if (part instanceof TextPart textPart) {
                            inputs.add(textPart.text());
                            logger.debug("Text Part: {}", textPart.text());
                        }
                    }
                }

                logger.debug(
                    "Estimating value for {} {} {}",
                    inputs.get(0),
                    inputs.get(1),
                    inputs.get(2)
                );

                // Call the pricing agent with all parameters
                String agentResponse = pricingAgent.estimateValue(
                    inputs.get(0),                      // carMake
                    inputs.get(1),                      // carModel
                    Integer.parseInt(inputs.get(2)),    // carYear
                    inputs.get(3),                      // carCondition
                    inputs.get(4)                       // feedback
                );

                logger.debug("PricingAgent response: {}", agentResponse);

                // Return the result
                TextPart responsePart = new TextPart(agentResponse, null);
                List<Part<?>> parts = List.of(responsePart);

                // Add the response as an artifact and complete the task
                agentEmitter.addArtifact(parts);
                agentEmitter.complete();
            }

            @Override
            public void cancel(RequestContext context, AgentEmitter agentEmitter) throws A2AError {
                Task task = context.getTask();

                if (task.status().state() == TaskState.TASK_STATE_CANCELED) {
                    // task already cancelled
                    throw new TaskNotCancelableError();
                }

                if (task.status().state() == TaskState.TASK_STATE_COMPLETED) {
                    // task already completed
                    throw new TaskNotCancelableError();
                }

                // Cancel the task
                agentEmitter.cancel();
            }
        };
    }
}