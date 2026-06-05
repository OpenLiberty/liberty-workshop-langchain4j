package com.carmanagement.resource;

import com.carmanagement.models.ApprovalProposal;
import com.carmanagement.services.ApprovalService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST resource for managing approval proposals.
 * Provides endpoints for humans to view and approve/reject proposals.
 */
@Path("/approvals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApprovalResource {
    @Inject
    private Logger logger;

    @Inject
    ApprovalService approvalService;

    /**
     * Get all pending approval proposals.
     * This is called by the UI to display proposals awaiting human decision.
     */
    @GET
    @Path("/pending")
    public List<ApprovalProposal> getPendingProposals() {
        return approvalService.getPendingProposals();
    }

    /**
     * Get a specific proposal by ID.
     */
    @GET
    @Path("/{proposalId}")
    public Response getProposal(@PathParam("proposalId") Integer proposalId) {
        ApprovalProposal proposal = approvalService.getProposal(proposalId);
        if (proposal == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "Proposal not found"))
                .build();
        }
        return Response.ok(proposal).build();
    }

    /**
     * Approve a proposal.
     * This is called when a human clicks the "Approve" button in the UI.
     * 
     * @param proposalId The proposal ID
     * @param request Request body containing reason and approvedBy
     */
    @POST
    @Path("/{proposalId}/approve")
    public Response approveProposal(
        @PathParam("proposalId") Integer proposalId,
        Map<String, String> request
    ) {
        
        try {
            String reason = request.getOrDefault("reason", "Approved by human reviewer");
            String approvedBy = request.getOrDefault("approvedBy", "Workshop User");

            logger.info("Approval request received for proposal {} by {}", proposalId, approvedBy);

            ApprovalProposal proposal = approvalService.processDecision(
                proposalId,
                true,
                reason,
                approvedBy
            );

            return Response.ok(proposal).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.error("Error approving proposal", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error processing approval: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Reject a proposal.
     * This is called when a human clicks the "Reject" button in the UI.
     *
     * @param proposalId The proposal ID
     * @param request Request body containing reason and approvedBy
     */
    @POST
    @Path("/{proposalId}/reject")
    public Response rejectProposal(
        @PathParam("proposalId") Integer proposalId,
        Map<String, String> request
    ) {
        
        try {
            String reason = request.getOrDefault("reason", "Rejected by human reviewer");
            String approvedBy = request.getOrDefault("approvedBy", "Workshop User");

            logger.info("Rejection request received for proposal {} by {}", proposalId, approvedBy);

            ApprovalProposal proposal = approvalService.processDecision(
                proposalId,
                false,
                reason,
                approvedBy
            );

            return Response.ok(proposal).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.error("Error rejecting proposal", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error processing rejection: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Make a decision on a proposal with explicit KEEP_CAR or DISPOSE_CAR action.
     * Simplified approach where the UI directly specifies the desired outcome.
     *
     * @param proposalId The proposal ID
     * @param request Request body containing decision (KEEP_CAR or DISPOSE_CAR), reason, and approvedBy
     */
    @POST
    @Path("/{proposalId}/decide")
    public Response decideProposal(
        @PathParam("proposalId") Integer proposalId,
        Map<String, String> request
    ) {
        
        try {
            String decision = request.get("decision"); // KEEP_CAR or DISPOSE_CAR
            String reason = request.getOrDefault("reason", "Decision by human reviewer");
            String approvedBy = request.getOrDefault("approvedBy", "Workshop User");

            if (decision == null || (!decision.equals("KEEP_CAR") && !decision.equals("DISPOSE_CAR"))) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Decision must be either KEEP_CAR or DISPOSE_CAR"))
                    .build();
            }

            logger.info("Decision '{}' received for proposal {} by {}", decision, proposalId, approvedBy);

            // Store the decision in the reason so the workflow can use it
            String fullReason = decision + ": " + reason;
            
            // We still use the approve/reject mechanism, but the decision is in the reason
            ApprovalProposal proposal = approvalService.processDecision(
                proposalId,
                true,
                fullReason,
                approvedBy
            );

            return Response.ok(proposal).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.error("Error processing decision", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error processing decision: " + e.getMessage()))
                .build();
        }
    }
}
