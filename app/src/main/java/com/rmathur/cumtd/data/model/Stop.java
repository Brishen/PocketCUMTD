package com.rmathur.cumtd.data.model;

public class Stop {
    private long id;
    private String stopId;
    private String stopName;
    private double latitude;
    private double longitude;

    public Stop() {
        id = 0;
        stopId = "";
        stopName = "";
        latitude = 0;
        longitude = 0;
    }

    public Stop(String idOfStop, String nameOfStop, double latOfStop, double longOfStop) {
        id = 0;
        stopId = idOfStop;
        stopName = nameOfStop;
        latitude = latOfStop;
        longitude = longOfStop;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String newStopName) {
        this.stopName = newStopName;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return stopName;
    }
} 