package com.carmanagement.agentic.executors;

import com.carmanagement.agentic.agents.PricingAgent;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.tasks.TaskUpdater;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.TextPart;
import io.a2a.spec.UnsupportedOperationError;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

/**
 * Executor for the PricingAgent.
 * Handles the integration between the A2A framework and the PricingAgent.
 */
@ApplicationScoped
public class PricingAgentExecutor {
    @Inject
    private Logger logger;

    @Produces
    public AgentExecutor agentExecutor(PricingAgent pricingAgent) {
        return new AgentExecutor() {
            @Override
            public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
                logger.info("Remote A2A PricingAgent called");

                TaskUpdater updater = new TaskUpdater(context, eventQueue);
                if (context.getTask() == null) {
                    updater.submit();
                }
                updater.startWork();

                List<String> inputs = new ArrayList<>();

                // Process the request message
                Message message = context.getMessage();
                if (message.getParts() != null) {
                    for (Part<?> part : message.getParts()) {
                        if (part instanceof TextPart textPart) {
                            inputs.add(textPart.getText());
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
                updater.addArtifact(parts, null, null, null);
                updater.complete();
            }

            @Override
            public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
                throw new UnsupportedOperationError();
            }
        };
    }
}