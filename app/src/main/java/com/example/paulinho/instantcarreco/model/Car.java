package com.example.paulinho.instantcarreco.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by paulinho on 11/24/2017.
 */

@IgnoreExtraProperties
public class Car {
    private String id;
    private String manufacture;
    private String year;
    private String comment;
    private String model;
    private Date date;
    private int rating;
    private String image;

    public Car(){}

    public Car(String manufacture, String year, String comment, String model, String image) {
        this.manufacture = manufacture;
        this.year = year;
        this.image = image;
        this.comment = comment;
        this.model = model;
        this.date = new Date();
        this.rating = 0;
    }
    public Car(String id, String manufacture, String year, String comment, String model, String image) {
        this.manufacture = manufacture;
        this.year = year;
        this.image = image;
        this.id = id;
        this.comment = comment;
        this.model = model;
        this.date = new Date();
        this.rating = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getManufacture() {
        return manufacture;
    }

    public void setManufacture(String manufacture) {
        this.manufacture = manufacture;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
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


    public Date getDate() {
        return date;
    }

    public String getDateFormat(){
        DateFormat dateFormat;
        dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }


    @Override
    public String toString() {

        return "Car:" + '\n' +
                "Manufacture: " + manufacture + '\n' +
                "model: " + model + '\n' +
                "year: " + year + '\n' +
                "date: " +getDateFormat()+ '\n' +
                "comment: " + comment;
    }
}
