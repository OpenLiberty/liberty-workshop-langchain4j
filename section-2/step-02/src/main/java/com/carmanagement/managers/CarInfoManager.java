package com.carmanagement.managers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carmanagement.exceptions.CarInfoNotFoundException;
import com.carmanagement.models.CarInfo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class CarInfoManager {
    private static final Logger logger = LoggerFactory.getLogger(CarInfoManager.class);

    @PersistenceContext
    private EntityManager em;

    public List<CarInfo> getCars(int offset, int limit) {
        return em.createNamedQuery("CarInfo.getCars", CarInfo.class)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }

    public CarInfo getCar(int carInfoId) throws CarInfoNotFoundException {
        try {
            var carInfo = em.find(CarInfo.class, carInfoId);
            if (carInfo == null) {
                throw new CarInfoNotFoundException(carInfoId);    
            }
            return carInfo;
        } catch (NoResultException e) {
            logger.debug("CarInfo {} does not exist", carInfoId);
            throw new CarInfoNotFoundException(carInfoId);
        }
    }

    public void updateCar(CarInfo carInfo) {
        logger.info("Updating car info: {}", carInfo);

        // Merge the detached entity back into the persistence context
        em.merge(carInfo);
    }
}
