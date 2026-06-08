package dev.langchain4j.workshop;

import dev.langchain4j.workshop.Exceptions.BookingNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class BookingManager {
    private static final Logger logger = LoggerFactory.getLogger(BookingManager.class);

    @PersistenceContext
    private EntityManager em;

    public List<Booking> getBookings(int offset, int limit) {
        return em.createNamedQuery("Booking.getBookings", Booking.class)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }

    public List<Booking> findBookingsByCustomer(
        String firstName,
        String lastName,
        int offset,
        int limit
    ) {
        return em.createNamedQuery("Booking.findBookingsByCustomer", Booking.class)
            .setParameter("firstName", firstName)
            .setParameter("lastName", lastName)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }

    public Booking getBooking(int bookingId) throws BookingNotFoundException {
        try {
            var booking = em.createNamedQuery("Booking.getBooking", Booking.class)
                .setParameter("id", bookingId)
                .getSingleResult();
            return booking;
        } catch (NoResultException e) {
            logger.debug("Booking {} does not exist", bookingId);
            throw new BookingNotFoundException(bookingId);
        }
    }

    @Transactional
    public void deleteBooking(int bookingId) {
        var booking = em.find(Booking.class, bookingId);
        if (booking == null) {
            throw new BookingNotFoundException(bookingId);
        }
        em.remove(booking);
    }
}
