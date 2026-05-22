package com.carmanagement.models;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Model class representing a car in the rental fleet.
 */
@Entity(name = "CarInfo")
@Table(name="car_info")
@SequenceGenerator(name = "CarInfoIdSequence", sequenceName = "car_info_id_seq")
@NamedQuery(name = "CarInfo.getCars", query = "SELECT c FROM CarInfo c")
@NamedQuery(name = "CarInfo.getCar", query = "SELECT c FROM CarInfo c WHERE c.id = :id")
public class CarInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CarInfoIdSequence")
    @Column(name = "id", nullable = false)
    private int id;

    private String make;
    private String model;
    private Integer year;

    @Enumerated(EnumType.STRING)
    private CarStatus status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMake() {
        return make;
    }
    
    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }   

    public void setYear(Integer year) {
        this.year = year;
    }

    public CarStatus getStatus() {
        return status;
    }   

    public void setStatus(CarStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CarInfo {"
        + "  id=" + id + ",\n"
        + "  make=" + make + ",\n"
        + "  model=" + model + ",\n"
        + "  year=" + year + ",\n"
        + "  status=" + status + "\n"
        + "}";
    }
}
