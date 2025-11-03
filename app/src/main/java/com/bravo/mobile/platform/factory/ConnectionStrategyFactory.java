package com.bravo.mobile.platform.factory;

import android.content.Context;

import com.bravo.mobile.platform.PlatformDetector;
import com.bravo.mobile.platform.PlatformType;
import com.bravo.mobile.platform.interfaces.IConnectionStrategy;
import com.bravo.mobile.platform.android.AndroidBLEStrategy;
import com.bravo.mobile.platform.android.AndroidUSBStrategy;

/**
 * Factory for creating platform-specific connection strategies
 * Implements the Strategy pattern to provide appropriate implementations
 * based on the current platform and connection type
 */
public class ConnectionStrategyFactory {
    
    /**
     * Create a BLE connection strategy for the current platform
     * @param context Application or activity context (Android-specific, can be null for iOS)
     * @return Platform-specific BLE connection strategy
     */
    public static IConnectionStrategy createBLEStrategy(Context context) {
        PlatformType platform = PlatformDetector.detectPlatform();
        
        switch (platform) {
            case ANDROID:
                return new AndroidBLEStrategy(context);
            
            case IOS:
                // iOS implementation would go here
                // return new IOSBLEStrategy();
                throw new UnsupportedOperationException("iOS BLE strategy not yet implemented");
            
            default:
                throw new UnsupportedOperationException("Unsupported platform: " + platform);
        }
    }
    
    /**
     * Create a USB connection strategy for the current platform
     * @param context Application or activity context (Android-specific, can be null for iOS)
     * @return Platform-specific USB connection strategy
     */
    public static IConnectionStrategy createUSBStrategy(Context context) {
        PlatformType platform = PlatformDetector.detectPlatform();
        
        switch (platform) {
            case ANDROID:
                return new AndroidUSBStrategy(context);
            
            case IOS:
                // iOS implementation would go here
                // Note: iOS has limited USB host support, primarily through MFi accessories
                // return new IOSUSBStrategy();
                throw new UnsupportedOperationException("iOS USB strategy not yet implemented");
            
            default:
                throw new UnsupportedOperationException("Unsupported platform: " + platform);
        }
    }
    
    /**
     * Create a connection strategy based on type
     * @param context Application or activity context
     * @param connectionType Type of connection (BLE or USB)
     * @return Platform-specific connection strategy
     */
    public static IConnectionStrategy createStrategy(Context context, 
                                                     IConnectionStrategy.ConnectionType connectionType) {
        switch (connectionType) {
            case BLUETOOTH_LOW_ENERGY:
                return createBLEStrategy(context);
            
            case USB_SERIAL:
                return createUSBStrategy(context);
            
            default:
                throw new IllegalArgumentException("Unknown connection type: " + connectionType);
        }
    }
}
