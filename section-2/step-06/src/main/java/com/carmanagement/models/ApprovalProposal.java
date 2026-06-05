package com.carmanagement.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entity representing a disposition proposal awaiting human approval.
 * This is the core of the Human-in-the-Loop pattern - proposals are stored
 * and the workflow pauses until a human makes an approval decision.
 */
@Entity(name = "ApprovalProposal")
@Table(name="approval_proposal")
@NamedQuery(name = "ApprovalProposal.findPendingByCarNumber", query = "SELECT ap FROM ApprovalProposal ap WHERE ap.carNumber = :id AND ap.status = :status")
@NamedQuery(name = "ApprovalProposal.findAllPending", query = "SELECT ap FROM ApprovalProposal ap WHERE ap.status = :status")
public class ApprovalProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ApprovalProposalIdSequence")
    @Column(name = "id", nullable = false)
    private int id;

    /**
     * The car number this proposal is for
     */
    @Column(nullable = false)
    private Integer carNumber;

    /**
     * Car make
     */
    private String carMake;

    /**
     * Car model
     */
    private String carModel;

    /**
     * Car year
     */
    private Integer carYear;

    /**
     * Estimated car value
     */
    private String carValue;

    /**
     * Proposed disposition action (SCRAP, SELL, DONATE, KEEP)
     */
    @Column(nullable = false)
    private String proposedDisposition;

    /**
     * Reasoning for the proposed disposition
     */
    @Column(length = 2000)
    private String dispositionReason;

    /**
     * Current car condition
     */
    @Column(length = 1000)
    private String carCondition;

    /**
     * Rental feedback that triggered this proposal
     */
    @Column(length = 2000)
    private String rentalFeedback;

    /**
     * Current status of the approval
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    /**
     * Human's decision (APPROVED or REJECTED)
     */
    private String decision;

    /**
     * Human's reasoning for their decision
     */
    @Column(length = 1000)
    private String approvalReason;

    /**
     * Who approved/rejected (for audit trail)
     */
    private String approvedBy;

    /**
     * When the proposal was created
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * When the decision was made
     */
    private LocalDateTime decidedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(Integer carNumber) {   
        this.carNumber = carNumber; 
    }

    public String getCarMake() {
        return carMake;
    }
    
    public void setCarMake(String make) {
        this.carMake = make;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String model) {
        this.carModel = model;
    }

    public Integer getCarYear() {
        return carYear;
    }   

    public void setCarYear(Integer year) {
        this.carYear = year;
    }

    public String getCarValue() {
        return carValue;
    }
    
    public void setCarValue(String value) {
        this.carValue = value;
    }

    public String getProposedDisposition() {
        return proposedDisposition;
    }
    
    public void setProposedDisposition(String proposedDisposition) {
        this.proposedDisposition = proposedDisposition;
    }

    public String getDispositionReason() {
        return dispositionReason;
    }
    
    public void setDispositionReason(String dispositionReason) {
        this.dispositionReason = dispositionReason;
    }

    public String getCarCondition() {
        return carCondition;
    }

    public void setCarCondition(String condition) {
        this.carCondition = condition;
    }

    public String getRentalFeedback() {
        return rentalFeedback;
    }

    public void setRentalFeedback(String feedback) {
        this.rentalFeedback = feedback;
    }

    public ApprovalStatus getStatus() {
        return status;
    }   

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getApprovalReason() {
        return approvalReason;
    }

    public void setApprovalReason(String reason) {
        this.approvalReason = reason;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }
}