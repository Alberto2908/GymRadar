package com.example.gyms;

public class Gym {

    private String name;
    private double lat;
    private double lng;
    private String address;
    private double distance;

    // Constructor
    public Gym(String name, double lat, double lng, String address, double distance) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        this.distance = distance;
    }

    // Getters y Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getDistance() {
        return distance;
    }

    public String getDistanceKm() {
        return String.format("%.2f km", distance);
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "Gimnasio{" +
                "name='" + name + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", address=" + address +
                ", distance=" + distance +
                '}';
    }
}
