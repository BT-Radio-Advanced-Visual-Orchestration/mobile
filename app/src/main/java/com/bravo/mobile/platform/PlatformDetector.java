package com.bravo.mobile.platform;

/**
 * Utility class to detect the current platform
 * Uses system properties to determine if running on Android or iOS
 */
public class PlatformDetector {
    
    private static PlatformType cachedPlatform = null;
    
    /**
     * Detect the current platform
     * @return PlatformType enum value
     */
    public static PlatformType detectPlatform() {
        if (cachedPlatform != null) {
            return cachedPlatform;
        }
        
        // Check for Android
        String javaVendor = System.getProperty("java.vendor", "");
        String javaVmVendor = System.getProperty("java.vm.vendor", "");
        String javaVmName = System.getProperty("java.vm.name", "");
        
        if (javaVendor.toLowerCase().contains("android") || 
            javaVmVendor.toLowerCase().contains("android") ||
            javaVmName.toLowerCase().contains("dalvik") ||
            javaVmName.toLowerCase().contains("art")) {
            cachedPlatform = PlatformType.ANDROID;
            return cachedPlatform;
        }
        
        // Check for iOS (when running through J2ObjC or similar)
        String osName = System.getProperty("os.name", "");
        if (osName.toLowerCase().contains("ios") || 
            osName.toLowerCase().contains("iphone") ||
            osName.toLowerCase().contains("ipad")) {
            cachedPlatform = PlatformType.IOS;
            return cachedPlatform;
        }
        
        // Try to detect by class availability
        try {
            Class.forName("android.os.Build");
            cachedPlatform = PlatformType.ANDROID;
            return cachedPlatform;
        } catch (ClassNotFoundException e) {
            // Not Android
        }
        
        cachedPlatform = PlatformType.UNKNOWN;
        return cachedPlatform;
    }
    
    /**
     * Check if running on Android
     * @return true if on Android
     */
    public static boolean isAndroid() {
        return detectPlatform() == PlatformType.ANDROID;
    }
    
    /**
     * Check if running on iOS
     * @return true if on iOS
     */
    public static boolean isIOS() {
        return detectPlatform() == PlatformType.IOS;
    }
    
    /**
     * Get platform name as string
     * @return Platform name
     */
    public static String getPlatformName() {
        return detectPlatform().name();
    }
}
