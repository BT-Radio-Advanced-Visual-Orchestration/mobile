package com.bravo.mobile.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.bravo.mobile.models.TelemetryData;
import com.bravo.mobile.platform.factory.ConnectionStrategyFactory;
import com.bravo.mobile.platform.interfaces.IConnectionListener;
import com.bravo.mobile.platform.interfaces.IConnectionStrategy;

/**
 * Refactored foreground service for managing BLE connection to ESP32 collar/dongle
 * Uses strategy pattern for platform-agnostic connection management
 * Receives LoRa telemetry data via BLE and broadcasts it to the app
 */
public class BLEConnectionService extends Service {
    private static final String CHANNEL_ID = "BLEConnectionChannel";
    private static final int NOTIFICATION_ID = 1;
    
    public static final String ACTION_DATA_RECEIVED = "com.bravo.mobile.ACTION_DATA_RECEIVED";
    public static final String EXTRA_TELEMETRY = "telemetry_data";
    
    private IConnectionStrategy connectionStrategy;
    private final IBinder binder = new LocalBinder();
    private ConnectionStateListener connectionStateListener;

    public interface ConnectionStateListener {
        void onConnectionStateChanged(boolean connected);
        void onTelemetryReceived(TelemetryData data);
    }

    public class LocalBinder extends Binder {
        public BLEConnectionService getService() {
            return BLEConnectionService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Create BLE connection strategy using factory
        connectionStrategy = ConnectionStrategyFactory.createBLEStrategy(this);
        
        // Initialize the strategy
        connectionStrategy.initialize();
        
        // Set up listener for connection events
        connectionStrategy.setConnectionListener(new IConnectionListener() {
            @Override
            public void onConnectionStateChanged(boolean connected) {
                if (connectionStateListener != null) {
                    connectionStateListener.onConnectionStateChanged(connected);
                }
            }
            
            @Override
            public void onTelemetryReceived(TelemetryData data) {
                if (connectionStateListener != null) {
                    connectionStateListener.onTelemetryReceived(data);
                }
                broadcastTelemetryData(data);
            }
            
            @Override
            public void onError(String error) {
                // Log error or notify user
            }
        });
        
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotification("BLE Connection Service Running");
        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Connect to a BLE device by address
     */
    public boolean connectToDevice(String deviceAddress) {
        if (connectionStrategy == null) {
            return false;
        }
        return connectionStrategy.connect(deviceAddress);
    }

    /**
     * Disconnect from the BLE device
     */
    public void disconnect() {
        if (connectionStrategy != null) {
            connectionStrategy.disconnect();
        }
    }

    /**
     * Check if connected to a device
     */
    public boolean isConnected() {
        return connectionStrategy != null && connectionStrategy.isConnected();
    }

    /**
     * Set connection state listener
     */
    public void setConnectionStateListener(ConnectionStateListener listener) {
        this.connectionStateListener = listener;
    }



    /**
     * Broadcast telemetry data to the app
     */
    private void broadcastTelemetryData(TelemetryData telemetry) {
        Intent intent = new Intent(ACTION_DATA_RECEIVED);
        intent.putExtra(EXTRA_TELEMETRY, telemetry.toString());
        sendBroadcast(intent);
    }

    /**
     * Create notification channel for foreground service
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "BLE Connection Service",
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * Create notification for foreground service
     */
    private Notification createNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("BRAVO Mobile")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connectionStrategy != null) {
            connectionStrategy.cleanup();
        }
    }
}
