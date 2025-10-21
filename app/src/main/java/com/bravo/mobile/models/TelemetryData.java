package com.bravo.mobile.models;

import java.util.Date;

/**
 * Data model for LoRa telemetry received from ESP32 collar/dongle
 * Contains GPS location, signal strength, and other sensor data
 */
public class TelemetryData {
    private double latitude;
    private double longitude;
    private double altitude;
    private float speed;
    private int rssi; // Signal strength
    private int snr; // Signal-to-noise ratio
    private Date timestamp;
    private String deviceId;
    private int batteryLevel;

    public TelemetryData() {
        this.timestamp = new Date();
    }

    public TelemetryData(double latitude, double longitude, double altitude, float speed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = speed;
        this.timestamp = new Date();
    }

    // Getters and Setters
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

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getSnr() {
        return snr;
    }

    public void setSnr(int snr) {
        this.snr = snr;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    @Override
    public String toString() {
        return "TelemetryData{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", speed=" + speed +
                ", rssi=" + rssi +
                ", snr=" + snr +
                ", timestamp=" + timestamp +
                ", deviceId='" + deviceId + '\'' +
                ", batteryLevel=" + batteryLevel +
                '}';
    }
}
