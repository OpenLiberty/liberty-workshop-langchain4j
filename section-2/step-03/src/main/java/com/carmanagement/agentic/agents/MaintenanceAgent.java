package com.carmanagement.agentic.agents;

import dev.langchain4j.cdi.spi.RegisterSimpleAgent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Agent that determines what maintenance services to request.
 */
@RegisterSimpleAgent(
    name = "maintenance-agent",
    description = "Car maintenance specialist. Using car information and request, determines what maintenance services are needed.",
    chatModelName = "chat-model",
    chatMemoryName = "maintenance-agent-memory",
    outputKey = "analysisResult",
    scope = ApplicationScoped.class
)
public interface MaintenanceAgent {

    @SystemMessage("""
        You handle intake for the car maintenance department of a car rental company.
        Based on the maintenance request, determine what specific services are needed and provide a detailed maintenance plan.
        Be specific about what services are needed based on the maintenance request.

        Available maintenance services include:
        - Oil change
        - Tire rotation
        - Brake service
        - Engine service
        - Transmission service
        - Body work (dent repair, paint, collision repair)

        For body damage like dents, scratches, or collision damage, include body work in your plan.

        Provide your response as a structured maintenance plan listing the specific services needed.
    """)
    @UserMessage("""
        Car Information:
        Make: {{carMake}}
        Model: {{carModel}}
        Year: {{carYear}}
        Car Number: {{carNumber}}
        
        Maintenance Request:
        {{maintenanceRequest}}
    """)
    String processMaintenance(
        @V("carMake") String carMake,
        @V("carModel") String carModel,
        @V("carYear") Integer carYear,
        @V("carNumber") Integer carNumber,
        @V("maintenanceRequest") String maintenanceRequest
    );
}
