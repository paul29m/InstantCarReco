package com.example.paulinho.instantcarreco.model;

import com.google.firebase.database.IgnoreExtraProperties;


@IgnoreExtraProperties
public class Owner {
    private String id;
    private String userId;
    private String carId;

    public Owner() {
    }

    public Owner(String id, String userId, String carId) {
        this.id = id;
        this.userId = userId;
        this.carId = carId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }
}
