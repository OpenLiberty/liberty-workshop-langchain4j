package dev.langchain4j.workshop;

import dev.langchain4j.workshop.Exceptions.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CustomerManager {
    private static final Logger logger = LoggerFactory.getLogger(CustomerManager.class);

    @PersistenceContext
    private EntityManager em;

    public List<Customer> getCustomers(int offset, int limit) {
        return em.createNamedQuery("Customer.getCustomers", Customer.class)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }

    public Customer findByFirstAndLastName(
        String firstName,
        String lastName
    ) throws CustomerNotFoundException {
        try {
            var customer = em.createNamedQuery("Customer.findByFirstAndLastName", Customer.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .getSingleResult();
            return customer;
        } catch (NoResultException e) {
            logger.debug("Customer {} {} does not exist", firstName, lastName);
            throw new CustomerNotFoundException(firstName, lastName);
        }
    }
}
