package dev.langchain4j.workshop;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity(name = "Customer")
@Table(name="customers")
@SequenceGenerator(name = "CustomerIdSequence", sequenceName = "customer_seq")
@NamedQuery(name = "Customer.getCustomers", query = "SELECT c FROM Customer c")
@NamedQuery(name = "Customer.findByFirstAndLastName", query = "SELECT c FROM Customer c WHERE c.firstName = :firstName AND c.lastName = :lastName")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CustomerIdSequence")
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}