package com.example.taller3.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Interes {
    private LatLng location;
    private String name;

    public Interes(LatLng pLocation, String pName){
        this.location = pLocation;
        this.name = pName;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
