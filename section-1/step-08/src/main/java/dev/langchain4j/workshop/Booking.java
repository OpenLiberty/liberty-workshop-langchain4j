package dev.langchain4j.workshop;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Entity(name = "Booking")
@Table(name="bookings")
@SequenceGenerator(name = "BookingIdSequence", sequenceName = "booking_seq")
@NamedQuery(name = "Booking.getBookings", query = "SELECT b FROM Booking b")
@NamedQuery(name = "Booking.getBooking", query = "SELECT b FROM Booking b WHERE b.id = :id")
@NamedQuery(name = "Booking.findBookingsByCustomer", query = "SELECT b FROM Booking b WHERE b.customer.firstName = :firstName AND b.customer.lastName = :lastName")
public class Booking implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BookingIdSequence")
    @Column(name = "id", nullable = false)
    private int id;

    @ManyToOne
    Customer customer;
    LocalDate dateFrom;
    LocalDate dateTo;
    String location;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}