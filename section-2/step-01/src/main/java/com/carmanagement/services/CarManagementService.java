package com.carmanagement.services;

import org.slf4j.Logger;

import com.carmanagement.agentic.agents.CleaningAgent;
import com.carmanagement.managers.CarInfoManager;
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
    private CleaningAgent cleaningAgent;

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

            // Process the car result
            String result = cleaningAgent.processCleaning(
                carInfo.getMake(),
                carInfo.getModel(),
                carInfo.getYear(),
                carNumber,
                feedback
            );

            if (result.toUpperCase().contains("CLEANING_NOT_REQUIRED")) {
                carInfo.setStatus(CarStatus.AVAILABLE);
                carInfoManager.updateCar(carInfo);
            }

            return result;
        } catch (Exception e) {
            logger.error("Error procesing car return: {}", e.getMessage(), e);
            throw e;
        }
    }
}
