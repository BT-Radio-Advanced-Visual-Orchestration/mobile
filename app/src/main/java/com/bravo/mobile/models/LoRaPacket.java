package com.bravo.mobile.models;

/**
 * Represents a raw LoRa packet received from ESP32
 * This is parsed into TelemetryData
 */
public class LoRaPacket {
    private byte[] rawData;
    private int rssi;
    private int snr;
    private long frequency;
    private int spreadingFactor;
    private long timestamp;

    public LoRaPacket() {
        this.timestamp = System.currentTimeMillis();
    }

    public LoRaPacket(byte[] rawData, int rssi, int snr) {
        this.rawData = rawData;
        this.rssi = rssi;
        this.snr = snr;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
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

    public long getFrequency() {
        return frequency;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    public int getSpreadingFactor() {
        return spreadingFactor;
    }

    public void setSpreadingFactor(int spreadingFactor) {
        this.spreadingFactor = spreadingFactor;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
