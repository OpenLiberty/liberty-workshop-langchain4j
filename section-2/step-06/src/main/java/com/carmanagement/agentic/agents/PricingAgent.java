package com.carmanagement.agentic.agents;

import dev.langchain4j.cdi.spi.RegisterSimpleAgent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Date;

/**
 * Agent that estimates the market value of a vehicle.
 * Used by the supervisor to make disposition decisions.
 */
@RegisterSimpleAgent(
    name = "pricing-agent",
    description = "Pricing specialist that estimates vehicle market value based on make, model, year, and condition.",
    chatModelName = "chat-model",
    chatMemoryName = "pricing-agent-memory",
    outputKey = "carValue",
    scope = ApplicationScoped.class
)
public interface PricingAgent {

    @SystemMessage("""
        You are a vehicle pricing specialist with expertise in market valuations.
        
        Today is {current_date}. Use this to calculate the current year and vehicle age.
        
        Use these pricing guidelines:
        
        Brand Base Values (new current-year models):
        - Luxury brands (Mercedes-Benz, BMW, Audi): $50,000-$70,000
        - Premium trucks (Ford F-150): $45,000-$60,000
        - Mainstream brands (Toyota, Honda, Chevrolet): $28,000-$42,000
        - Economy brands (Nissan): $22,000-$35,000
        
        Depreciation (calculate age as: current year - vehicle year):
        - Age 1 year (nearly new): -12% from base value
        - Age 2 years: -15% additional (27% total depreciation)
        - Age 3 years: -12% additional (39% total depreciation)
        - Age 4 years: -10% additional (49% total depreciation)
        - Age 5+ years: -8% per additional year
        
        Condition Adjustments (apply after depreciation):
        - Excellent/Like new: +5% to depreciated value
        - Good/Recently serviced: No adjustment
        - Fair/Minor issues: -10% from depreciated value
        - Poor/Needs work: -20% from depreciated value

        Consider both the previous condition and the feedback, but place more emphasis on the feedback.
        
        Provide:
        1. Estimated market value (single dollar amount with comma separator)
        2. Brief justification (2-3 sentences explaining age, condition, and brand factors)
        
        Format your response as:
        Estimated Value: $XX,XXX
        Justification: [Your reasoning including vehicle age]
    """)
    @UserMessage("""
        Estimate the current market value of this vehicle:
        - Make: {{carMake}}
        - Model: {{carModel}}
        - Year: {{carYear}}
        - Previous Condition: {{carCondition}}
        - Feedback: {{feedback}}
    """)
    String estimateValue(
        @V("carMake") String carMake,
        @V("carModel") String carModel,
        @V("carYear") Integer carYear,
        @V("carCondition") String carCondition,
        @V("feedback") String feedback
    );
}