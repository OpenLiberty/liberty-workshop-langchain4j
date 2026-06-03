package com.carmanagement.agentic.agents;

import dev.langchain4j.cdi.spi.RegisterSimpleAgent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Agent that analyzes feedback to determine if maintenance is needed.
 */
@RegisterSimpleAgent(
    name = "maintenance-feedback-agent",
    description = "Car maintenance analyzer. Using feedback, determines if a car needs maintenance.",
    chatModelName = "chat-model",
    chatMemoryName = "maintenance-feedback-agent-memory",
    outputKey = "maintenanceRequest",
    scope = ApplicationScoped.class
)
public interface MaintenanceFeedbackAgent {

    @SystemMessage("""
        You are a car maintenance analyzer for a car rental company. Your job is to determine if a car needs maintenance based on feedback.
        Analyze the feedback and car information to decide if maintenance is needed.
        Maintenance never includes any cleaning or detailing.
        If the feedback mentions mechanical issues, strange noises, performance problems, significant body damage or anything that suggests
        the car needs maintenance, recommend appropriate maintenance.
        Be specific about what type of maintenance is needed (oil change, tire rotation, brake service, engine service, transmission service, body work).
        If no service of any kind, repairs or maintenance are needed, respond with "MAINTENANCE_NOT_REQUIRED".
        Include the reason for your choice but keep your response short.
    """)
    @UserMessage("""
        Car Information:
        Make: {{carMake}}
        Model: {{carModel}}
        Year: {{carYear}}
        Previous Condition: {{carCondition}}
        
        Feedback: {{feedback}}
    """)
    String analyzeForMaintenance(
        @V("carMake") String carMake,
        @V("carModel") String carModel,
        @V("carYear") Integer carYear,
        @V("carNumber") Integer carNumber,
        @V("carCondition") String carCondition,
        @V("feedback") String feedback
    );
}
