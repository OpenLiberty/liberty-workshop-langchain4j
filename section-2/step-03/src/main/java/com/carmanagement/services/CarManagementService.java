package com.carmanagement.services;

import org.slf4j.Logger;

import com.carmanagement.agentic.workflows.CarProcessingWorkflow;
import com.carmanagement.managers.CarInfoManager;
import com.carmanagement.models.CarConditions;
import com.carmanagement.models.CarInfo;
import com.carmanagement.models.CarStatus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service for managing car returns from various operations.
 */
@ApplicationScoped
public class CarManagementService {
    @Inject
    private Logger logger;

    @Inject
    private CarInfoManager carInfoManager;

    @Inject
    CarProcessingWorkflow carProcessingWorkflow;

    /**
     * Process a car return from any operation.
     *
     * @param carNumber The car number
     * @param feedback Optional feedback
     * @return Result of the processing
     */
    @Transactional
    public String processCarReturn(Integer carNumber, String feedback) {
        try {
            CarInfo carInfo = carInfoManager.getCar(carNumber);
            if (carInfo == null) {
                return "Car not found with number: " + carNumber;
            }

            logger.info("FeedbackWorkflow executing...");
            logger.info("  ├─ CleaningFeedbackAgent analyzing...");
            logger.info("  └─ MaintenanceFeedbackAgent analyzing...");
            logger.info("CarAssignmentWorkflow evaluating conditions...");

            // Process the car return using the workflow and get the AgenticScope
            CarConditions carConditions = carProcessingWorkflow.processCarReturn(
                carInfo.getMake(),
                carInfo.getModel(),
                carInfo.getYear(),
                carNumber,
                carInfo.getCondition(),
                feedback
            );

            logger.info("CarConditionFeedbackAgent updating...");

            // Update the car's condition with the result from CarConditionFeedbackAgent
            carInfo.setCondition(carConditions.generalCondition());

            // Update the car status based on the required action
            switch (carConditions.carAssignment()) {
                case MAINTENANCE:
                    carInfo.setStatus(CarStatus.IN_MAINTENANCE);
                    break;
                case CLEANING:
                    carInfo.setStatus(CarStatus.AT_CLEANING);
                    break;
                case NONE:
                    carInfo.setStatus(CarStatus.AVAILABLE);
                    break;
            }

            carInfoManager.updateCar(carInfo);

            return carConditions.generalCondition();
        } catch (Exception e) {
            logger.error("Error procesing car return: {}", e.getMessage(), e);
            throw e;
        }
    }
}
