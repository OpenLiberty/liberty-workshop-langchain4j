package com.carmanagement.agentic.agents;

import dev.langchain4j.cdi.spi.RegisterSimpleAgent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Agent that determines how to dispose of a car based on value, condition, and damage.
 */
@RegisterSimpleAgent(
    name = "disposition-agent",
    description = "Car disposition specialist. Determines how to dispose of a car based on value and condition.",
    chatModelName = "chat-model",
    chatMemoryName = "disposition-agent-memory",
    outputKey = "dispositionAction",
    scope = ApplicationScoped.class
)
public interface DispositionAgent {

    @SystemMessage("""
        You are a car disposition specialist for a car rental company.
        Your job is to determine the best disposition action based on the car's value, condition, age, and damage.
        
        Disposition Options:
        - SCRAP: Car is beyond economical repair or has severe safety concerns
        - SELL: Car has value but is aging out of the fleet or has moderate damage
        - DONATE: Car has minimal value but could serve a charitable purpose
        - KEEP: Car is worth keeping in the fleet
        
        Decision Criteria:
        - If estimated repair cost > 50% of car value: Consider SCRAP or SELL
        - If car is over 5 years old with significant damage: SCRAP
        - If car is 3-5 years old in fair condition: SELL
        - If car has low value (<$5,000) but functional: DONATE
        - If car is valuable and damage is minor: KEEP
        
        Provide your recommendation with a clear explanation of the reasoning.

        CRITICAL: Never override a decision that has been made by a human.
    """)
    @UserMessage("""
        Determine the disposition for this vehicle:
        - Make: {{carMake}}
        - Model: {{carModel}}
        - Year: {{carYear}}
        - Car Number: {{carNumber}}
        - Current Condition: {{carCondition}}
        - Estimated Value: {{carValue}}
        - Damage/Feedback: {{feedback}}
        
        Provide your disposition recommendation (SCRAP/SELL/DONATE/KEEP) and explanation.
    """)
    String processDisposition(
        @V("carMake") String carMake,
        @V("carModel") String carModel,
        @V("carYear") Integer carYear,
        @V("carNumber") Integer carNumber,
        @V("carCondition") String carCondition,
        @V("carValue") String carValue,
        @V("feedback") String feedback
    );
}