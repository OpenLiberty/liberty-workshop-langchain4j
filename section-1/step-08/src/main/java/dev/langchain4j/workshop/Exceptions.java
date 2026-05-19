package dev.langchain4j.workshop;

public class Exceptions {

    public static class CustomerNotFoundException extends RuntimeException {
        public CustomerNotFoundException(String firstName, String lastName) {
            super("Customer not found: %s %s".formatted(firstName, lastName));
        }
    }

    public static class BookingCannotBeCancelledException extends RuntimeException {
        public BookingCannotBeCancelledException(long bookingId) {
            super("Booking %d cannot be cancelled - see terms of use".formatted(bookingId));
        }

        public BookingCannotBeCancelledException(long bookingId, String reason) {
            super("Booking %d cannot be cancelled because %s - see terms of use".formatted(bookingId, reason));
        }
    }

    public static class BookingNotFoundException extends RuntimeException {
        public BookingNotFoundException(long bookingId) {
            super("Booking %d not found".formatted(bookingId));
        }
    }
}