package com.bravo.mobile.platform.interfaces;

/**
 * Platform-agnostic interface for permission management
 * Handles runtime permissions for Bluetooth, Location, etc.
 */
public interface IPermissionManager {
    
    /**
     * Check if a specific permission is granted
     * @param permission Permission to check
     * @return true if permission is granted
     */
    boolean hasPermission(String permission);
    
    /**
     * Request permissions from the user
     * @param permissions Array of permissions to request
     * @param callback Callback to handle the result
     */
    void requestPermissions(String[] permissions, PermissionCallback callback);
    
    /**
     * Get platform-specific permission strings for Bluetooth
     * @return Array of Bluetooth permission strings
     */
    String[] getBluetoothPermissions();
    
    /**
     * Get platform-specific permission strings for Location
     * @return Array of Location permission strings
     */
    String[] getLocationPermissions();
    
    /**
     * Callback interface for permission requests
     */
    interface PermissionCallback {
        void onPermissionResult(boolean allGranted);
    }
}
