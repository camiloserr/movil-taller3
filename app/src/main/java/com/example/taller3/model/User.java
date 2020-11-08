package com.example.taller3.model;

import com.google.android.gms.maps.model.LatLng;

public class User {

    private String ID;
    private double lat;
    private double lon;
    private boolean availible;
    private String name;
    private String lastname;

    public User(String ID, double lat, double lon, boolean availible, String name, String lastname) {
        this.ID = ID;
        this.lat = lat;
        this.lon = lon;
        this.availible = availible;
        this.name = name;
        this.lastname = lastname;
    }

    public User() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public boolean isAvailible() {
        return availible;
    }

    public void setAvailible(boolean availible) {
        this.availible = availible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
