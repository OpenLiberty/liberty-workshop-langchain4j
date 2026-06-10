package com.carmanagement.agentic.agents;

import dev.langchain4j.cdi.spi.RegisterA2AAgent;

/**
 * Agent that estimates the market value of a vehicle.
 * Used by the supervisor to make disposition decisions.
 */
@RegisterA2AAgent(
    name = "pricing-agent",
    description = "Pricing specialist that estimates vehicle market value based on make, model, year, and condition.",
    a2aServerUrl = "http://localhost:8888",
    outputKey = "carValue"
)
public interface PricingAgent {
   String estimateValue(
        String carMake,
        String carModel,
        Integer carYear,
        String carCondition,
        String feedback
    );
}