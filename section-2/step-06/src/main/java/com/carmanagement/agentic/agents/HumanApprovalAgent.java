package com.carmanagement.agentic.agents;

import com.carmanagement.models.ApprovalProposal;
import com.carmanagement.services.ApprovalService;

import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.cdi.spi.RegisterHumanInTheLoopAgent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Named;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RegisterHumanInTheLoopAgent(
    name = "human-approval-agent",
    description = "Coordinates human approval for high-value vehicle dispositions using the requestHumanApproval tool.",
    outputKey = "approvalDecision",
    askUser = "reviewDispositionProposal"
)
public interface HumanApprovalAgent {

    static String reviewDispositionProposal(AgenticScope scope) {
        final Logger logger = LoggerFactory.getLogger(HumanApprovalAgent.class);

        final String carMake = (String)scope.readState("carMake");
        final String carModel = (String)scope.readState("carModel");
        final Integer carYear = (Integer)scope.readState("carYear");
        final Integer carNumber = (Integer)scope.readState("carNumber");
        final String carValue = (String)scope.readState("carValue");
        final String fullDispositionProposal = (String)scope.readState("dispositionProposal");
        final String dispositionProposal = getDispositionProposal(fullDispositionProposal);
        final String dispositionReason = getDispositionReasoning(fullDispositionProposal);
        final String carCondition = (String)scope.readState("carCondition");
        final String feedback = (String)scope.readState("feedback");

        logger.info(
            "🛑 HITL Tool: Creating approval proposal for car {} - {} {} {}",
            carNumber,
            carYear,
            carMake,
            carModel
        );
        logger.info("⏸️  WORKFLOW PAUSED - Waiting for human approval decision via UI");

        final ApprovalService approvalService = CDI.current().select(ApprovalService.class).get();

        try {
            // Create proposal and get CompletableFuture that completes when human decides
            CompletableFuture<ApprovalProposal> approvalFuture = approvalService.createProposalAndWaitForDecision(
                carNumber,
                carMake,
                carModel,
                carYear,
                carValue,
                dispositionProposal,
                dispositionReason,
                carCondition,
                feedback
            );

            // BLOCK HERE until human makes decision (with 5 minute timeout)
            ApprovalProposal result = approvalFuture.get(5, TimeUnit.MINUTES);

            logger.info("▶️  WORKFLOW RESUMED - Human decision received: {}", result.getDecision());

            String foo = String.format("""
                Human Decision: %s
                Reason: %s
                Approved By: %s
                Decision Time: %s
                """,
                result.getDecision(),
                result.getApprovalReason() != null ? result.getApprovalReason() : "No reason provided",
                result.getApprovedBy() != null ? result.getApprovedBy() : "Unknown",
                result.getDecidedAt() != null ? result.getDecidedAt().toString() : "Unknown"
            );

            logger.info("Response: {}", foo);

            // Format response for the agent
            return String.format("""
                Human Decision: %s
                Reason: %s
                Approved By: %s
                Decision Time: %s
                """,
                result.getDecision(),
                result.getApprovalReason() != null ? result.getApprovalReason() : "No reason provided",
                result.getApprovedBy() != null ? result.getApprovedBy() : "Unknown",
                result.getDecidedAt() != null ? result.getDecidedAt().toString() : "Unknown"
            );
        } catch (TimeoutException e) {
            logger.error("⏱️  TIMEOUT: No human decision received within 5 minutes, defaulting to REJECTED");
            return """
                Human Decision: REJECTED
                Reason: Timeout - No human decision received within 5 minutes. Defaulting to rejection for safety.
                Approved By: System (Timeout)
                """;
        } catch (Exception e) {
            logger.error("❌ ERROR: Failed to get human approval for car {}", carNumber, e);
            return String.format("""
                Human Decision: REJECTED
                Reason: Error occurred while waiting for human approval: %s
                Approved By: System (Error)
                """,
                e.getMessage()
            );
        }
    }

    static String getDispositionProposal(String proposal) {
        String dispositionProposal = "KEEP";
        Pattern pattern = Pattern.compile("__(.*?)__");
        Matcher matcher = pattern.matcher(proposal);
        if (matcher.find()) {
            dispositionProposal = matcher.group(1);
        }
        return dispositionProposal;
    }

    static String getDispositionReasoning(String proposal) {
        return proposal.substring(proposal.indexOf("Reasoning:"));
    }
}