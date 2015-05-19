package com.rmathur.cumtd.data.model;

public class Departure {

    private String busName;
    private String long_name;
    private String minsLeft;
    private String color;
    private double latitude;
    private double longitude;
    private String shape;

    public Departure() {
        this.busName = "Bus";
        this.minsLeft = "0";
        this.color = "";
        this.latitude = 0;
        this.longitude = 0;
    }

    public Departure(String name, String longnamenew, String minutes, String colorString, double latitude, double longitude, String route_shape) {
        this.busName = name;
        this.long_name = longnamenew;
        this.minsLeft = minutes;
        this.color = colorString;
        this.latitude = latitude;
        this.longitude = longitude;
        this.shape = route_shape;
    }

    public String getBusName() {
        return this.busName;
    }

    public String getLongName() {
        return this.long_name;
    }

    public String getMinsLeft() {
        return this.minsLeft;
    }

    public String getColor() {
        return this.color;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String getShape() {
        return this.shape;
    }
}
