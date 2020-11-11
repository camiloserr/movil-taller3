package com.example.taller3.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Interes {

    private double latitude;
    private double longitude;
    private String name;

    public Interes(double plongitude,double platitude, String pName){
        this.latitude = platitude;
        this.longitude = plongitude;
        this.name = pName;
    }
    public Interes(){

    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
