package com.carmanagement.services;

import static dev.langchain4j.agentic.observability.HtmlReportGenerator.generateReport;

import com.carmanagement.agentic.workflows.CarProcessingWorkflow;
import com.carmanagement.managers.CarInfoManager;
import com.carmanagement.models.CarConditions;
import com.carmanagement.models.CarInfo;
import com.carmanagement.models.CarStatus;
import com.carmanagement.models.FeedbackTask;

import dev.langchain4j.data.message.ImageContent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

import org.slf4j.Logger;

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
     * @param carImage Optional image of the car
     * @return Result of the processing
     */
    @Transactional
    public String processCarReturn(
        Integer carNumber,
        String feedback,
        ImageContent carImage
    ) {
        try {
            CarInfo carInfo = carInfoManager.getCar(carNumber);
            if (carInfo == null) {
                return "Car not found with number: " + carNumber;
            }

            // Create the list of feedback tasks
            List<String> tasks = List.of(
                FeedbackTask.cleaning().systemInstructions(),
                FeedbackTask.maintenance().systemInstructions(),
                FeedbackTask.disposition().systemInstructions()
            );

            // Process the car return using the workflow with supervisor
            CarConditions carConditions = carProcessingWorkflow.processCarReturn(
                tasks,
                carInfo.getMake(),
                carInfo.getModel(),
                carInfo.getYear(),
                carNumber,
                carInfo.getCondition(),
                feedback,
                carImage
            );

            logger.info("CarConditionFeedbackAgent updating...");

            // Update the car's condition with the result from CarConditionFeedbackAgent
            carInfo.setCondition(carConditions.generalCondition());

            // Update the car status based on the required action
            switch (carConditions.carAssignment()) {
                case DISPOSITION:
                    carInfo.setStatus(CarStatus.PENDING_DISPOSITION);
                    logger.info("Car marked for disposition - awaiting final decision");
                    break;
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

    public String report() {
        return generateReport(carProcessingWorkflow.agentMonitor());
    }
}
