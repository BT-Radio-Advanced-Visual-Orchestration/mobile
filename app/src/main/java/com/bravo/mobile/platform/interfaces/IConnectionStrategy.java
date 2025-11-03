package com.bravo.mobile.platform.interfaces;

import com.bravo.mobile.models.TelemetryData;

/**
 * Platform-agnostic interface for device connection strategies
 * Supports both BLE and USB connections across different platforms
 */
public interface IConnectionStrategy {
    
    /**
     * Initialize the connection strategy
     * @return true if initialization was successful
     */
    boolean initialize();
    
    /**
     * Connect to a device
     * @param deviceIdentifier Device identifier (address for BLE, path for USB)
     * @return true if connection initiated successfully
     */
    boolean connect(String deviceIdentifier);
    
    /**
     * Disconnect from the current device
     */
    void disconnect();
    
    /**
     * Check if currently connected to a device
     * @return true if connected
     */
    boolean isConnected();
    
    /**
     * Get the connection type
     * @return ConnectionType enum value
     */
    ConnectionType getConnectionType();
    
    /**
     * Set listener for connection events
     * @param listener Listener to receive connection events
     */
    void setConnectionListener(IConnectionListener listener);
    
    /**
     * Clean up resources
     */
    void cleanup();
    
    /**
     * Connection type enumeration
     */
    enum ConnectionType {
        BLUETOOTH_LOW_ENERGY,
        USB_SERIAL
    }
}
