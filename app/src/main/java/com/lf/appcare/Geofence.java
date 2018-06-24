package com.lf.appcare;

public class Geofence
{
    private double lat, lng;
    private float radius;

    public Geofence() {}

    Geofence (double lat, double lng, float radius)
    {
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public float getRadius() {
        return radius;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
