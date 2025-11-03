package com.bravo.mobile.platform.interfaces;

import com.bravo.mobile.models.TelemetryData;

/**
 * Platform-agnostic listener for connection events and data reception
 */
public interface IConnectionListener {
    
    /**
     * Called when connection state changes
     * @param connected true if connected, false if disconnected
     */
    void onConnectionStateChanged(boolean connected);
    
    /**
     * Called when telemetry data is received
     * @param data Telemetry data received from the device
     */
    void onTelemetryReceived(TelemetryData data);
    
    /**
     * Called when an error occurs
     * @param error Error message
     */
    void onError(String error);
}
