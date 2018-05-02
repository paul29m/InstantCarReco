package com.example.paulinho.instantcarreco.model;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by paulinho on 11/24/2017.
 */

public class Car {
    private int id;
    private String make;
    private String year;
    private String comment;
    private String model;
    private byte[] image;

    public Car(String make, String year, String comment, String model, byte[] image) {
        this.make = make;
        this.year = year;
        this.image = image;
        this.id = ThreadLocalRandom.current().nextInt(1, 10^10 + 1);
        this.comment = comment;
        this.model = model;
    }

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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public String toString() {
        return "Car:" +
                "Vehicle: " + make + '\n' +
                "year: " + year + '\n' +
                "model: " + model + '\n' +
                "comment: " + comment;
    }
}
