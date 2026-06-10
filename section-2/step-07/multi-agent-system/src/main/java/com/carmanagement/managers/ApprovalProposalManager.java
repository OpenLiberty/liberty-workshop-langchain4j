package com.carmanagement.managers;

import com.carmanagement.models.ApprovalProposal;
import com.carmanagement.models.ApprovalStatus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;

@ApplicationScoped
public class ApprovalProposalManager {
    @Inject
    private Logger logger;

    @PersistenceContext
    private EntityManager em;

    public ApprovalProposal create(
        Integer carNumber,
        String carMake,
        String carModel,
        Integer carYear,
        String carValue,
        String proposedDisposition,
        String dispositionReason,
        String carCondition,
        String rentalFeedback
    ) {
        // Create new proposal
        ApprovalProposal proposal = new ApprovalProposal();
        proposal.setCarNumber(carNumber);
        proposal.setCarMake(carMake);
        proposal.setCarModel(carModel);
        proposal.setCarYear(carYear);
        proposal.setCarValue(carValue);
        proposal.setProposedDisposition(proposedDisposition);
        proposal.setDispositionReason(dispositionReason);
        proposal.setCarCondition(carCondition);
        proposal.setRentalFeedback(rentalFeedback);
        proposal.setStatus(ApprovalStatus.PENDING);
        proposal.setCreatedAt(LocalDateTime.now());

        em.persist(proposal);
        em.flush();

        return findPendingByCarNumber(carNumber);
    }

    public void update(ApprovalProposal approvalProposal) {
        logger.info("Updating approval proposal: {}", approvalProposal);
        // Merge the detached entity back into the persistence context
        em.merge(approvalProposal);
    }

    public ApprovalProposal findById(int proposalId) {
        var approvalProposal = em.find(ApprovalProposal.class, proposalId);
        return approvalProposal;
    }

    /**
     * Find pending proposal for a specific car
     */
    public ApprovalProposal findPendingByCarNumber(Integer carNumber) {
        // Create the variable to return
        ApprovalProposal approvalProposal = null;

        try {
            approvalProposal = em.createNamedQuery("ApprovalProposal.findPendingByCarNumber", ApprovalProposal.class)
                .setParameter("id", carNumber)
                .setParameter("status", ApprovalStatus.PENDING)
                .getSingleResult();
        } catch (NoResultException e) {
            // Log the exception and return null
            logger.info("No approval proposal found for car number {}", carNumber);
        }

        return approvalProposal;
    }

    /**
     * Find all pending proposals
     */
    public List<ApprovalProposal> findAllPending() {
        return em.createNamedQuery("ApprovalProposal.findAllPending", ApprovalProposal.class)
            .setParameter("status", ApprovalStatus.PENDING)
            .getResultList();
    }
}
