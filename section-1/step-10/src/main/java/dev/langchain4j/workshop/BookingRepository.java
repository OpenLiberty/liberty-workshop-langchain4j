package dev.langchain4j.workshop;

import dev.langchain4j.workshop.Exceptions.*;

import java.time.LocalDate;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import dev.langchain4j.agent.tool.Tool;

@ApplicationScoped
public class BookingRepository {

    @Inject
    private BookingManager bookingManager;

    @Inject
    private CustomerManager customerManager;

    @Transactional
    // @Tool("Cancel a booking")
    @Tool(
        name = "Cancel a booking",
        value = "Cancel a specific booking for the specified customer. " +
                "The tool requires three parameters, the bookding id, the first and and last name of the customer."
    )
    public void cancelBooking(int bookingId, String customerFirstName, String customerLastName) {
        var booking = getBookingDetails(bookingId, customerFirstName, customerLastName);

        // Too late to cancel
        if (booking.getDateFrom().minusDays(11).isBefore(LocalDate.now())) {
            throw new BookingCannotBeCancelledException(bookingId, "booking from date is 11 days before today");
        }

        // Too short to cancel
        if (booking.getDateTo().minusDays(4).isBefore(booking.getDateFrom())) {
            throw new BookingCannotBeCancelledException(bookingId, "booking period is less than four days");
        }

        bookingManager.deleteBooking(booking.getId());
    }

    @Transactional
    // @Tool("List booking for a customer")
    @Tool(
        name = "List bookings for a customer",
        value = "Query the database for the bookings for the specified customer. " +
                "The tool requires two parameters, the first and and last name of the customer. "
    )
    public List<Booking> listBookingsForCustomer(String firstName, String lastName) {
        // Attempt to retrieve the customer with the specified first and last name
        var customer = customerManager.findByFirstAndLastName(firstName, lastName);

        // Now retrieve the bookings for the customer
        return bookingManager.findBookingsByCustomer(firstName, lastName, 0, 100);
    }

    @Transactional
    // @Tool("Get booking details")
    @Tool(
        name = "Get booking details",
        value = "Query the database for the details of a specific booking for the specified customer. " +
                "The tool requires three parameters, the bookding id, the first and and last name of the customer."
    )
    public Booking getBookingDetails(int bookingId, String customerFirstName, String customerLastName) {
        var booking = bookingManager.getBooking(bookingId);

        if (  !booking.getCustomer().getFirstName().equals(customerFirstName)
           || !booking.getCustomer().getLastName().equals(customerLastName)
           ) {
            throw new BookingNotFoundException(bookingId);
        }
        return booking;
    }
}