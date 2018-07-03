package com.lf.appcare;

public class GeofenceEmergency
{
    private String emergencyType;
    private double lat, lng;
    private float acc;

    public GeofenceEmergency() {}

    GeofenceEmergency(String emergencyType, double lat, double lng, float acc)
    {
        this.emergencyType = emergencyType;
        this.lat = lat;
        this.lng = lng;
        this.acc = acc;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public float getAcc() {
        return acc;
    }

    public void setEmergencyType(String emergencyType) {
        this.emergencyType = emergencyType;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setAcc(float acc) {
        this.acc = acc;
    }
}
