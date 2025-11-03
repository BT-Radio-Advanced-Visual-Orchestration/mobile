package com.bravo.mobile.platform.interfaces;

/**
 * Platform-agnostic interface for background services
 * Provides abstraction for foreground services (Android) and background tasks (iOS)
 */
public interface IPlatformService {
    
    /**
     * Start the service in the foreground
     * @param notificationTitle Title for the notification
     * @param notificationText Text for the notification
     */
    void startForeground(String notificationTitle, String notificationText);
    
    /**
     * Stop the foreground service
     */
    void stopForeground();
    
    /**
     * Update the service notification
     * @param title New notification title
     * @param text New notification text
     */
    void updateNotification(String title, String text);
    
    /**
     * Check if the service is running in foreground
     * @return true if running in foreground
     */
    boolean isForeground();
}
