package com.carmanagement.agentic.tools;

import org.slf4j.Logger;

import com.carmanagement.managers.CarInfoManager;
import com.carmanagement.models.CarInfo;
import com.carmanagement.models.CarStatus;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

// --8<-- [start:CleaningTool]
/**
 * Tool for requesting cleaning operations.
 */
@Named("cleaning-tool")
@RequestScoped
public class CleaningTool {
    @Inject
    private Logger logger;

    @Inject
    private CarInfoManager carInfoManager;

    /**
     * Requests a cleaning based on the provided parameters.
     *
     * @param carNumber The car number
     * @param carMake The car make
     * @param carModel The car model
     * @param carYear The car year
     * @param exteriorWash Whether to request exterior wash
     * @param interiorCleaning Whether to request interior cleaning
     * @param detailing Whether to request detailing
     * @param waxing Whether to request waxing
     * @param requestText The cleaning request text
     * @return A summary of the cleaning request
     */
    @Tool("Requests a cleaning with the specified options")
    @Transactional
    public String requestCleaning(
        Integer carNumber,
        String carMake,
        String carModel,
        Integer carYear,
        boolean exteriorWash,
        boolean interiorCleaning,
        boolean detailing,
        boolean waxing,
        String requestText
    ) {
        logger.info("Cleaning requested for car {} ({} {} {}): {}", carNumber, carYear, carMake, carModel, requestText);

        /*
         * In a real implementation, this would make an API call to a cleaning service or update a database with the
         * cleaning request
         */
        
        // Update car status to AT_CLEANING
        CarInfo carInfo = carInfoManager.getCar(carNumber);

        if (carInfo != null) {
            carInfo.setStatus(CarStatus.AT_CLEANING);
            logger.info("Updating status for car {} to {}", carNumber, carInfo.getStatus());
            carInfoManager.updateCar(carInfo);
        }
        
        var result = generateCleaningSummary(
            carNumber,
            carMake,
            carModel,
            carYear,
            exteriorWash,
            interiorCleaning,
            detailing,
            waxing,
            requestText
        );
        logger.info("\uD83D\uDE97 CleaningTool result: {}", result);
        return result;
    }
// --8<-- [end:CleaningTool]

    private String generateCleaningSummary(
        Integer carNumber,
        String carMake,
        String carModel,
        Integer carYear,
        boolean exteriorWash,
        boolean interiorCleaning,
        boolean detailing,
        boolean waxing,
        String requestText
    ) {

        var summary = new StringBuilder();
        summary.append("Cleaning requested for ")
            .append(carMake)
            .append(" ")
            .append(carModel)
            .append(" (")
            .append(carYear)
            .append("), Car #")
            .append(carNumber)
            .append(":\n");
        
        if (exteriorWash) {
            summary.append("- Exterior wash\n");
        }
        
        if (interiorCleaning) {
            summary.append("- Interior cleaning\n");
        }
        
        if (detailing) {
            summary.append("- Detailing\n");
        }
        
        if (waxing) {
            summary.append("- Waxing\n");
        }
        
        if (requestText != null && !requestText.isEmpty()) {
            summary.append("Additional notes: ").append(requestText);
        }
        
        return summary.toString();
    }
}
