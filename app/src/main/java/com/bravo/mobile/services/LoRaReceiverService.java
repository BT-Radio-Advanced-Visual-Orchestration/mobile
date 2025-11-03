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
 * Refactored foreground service for receiving LoRa telemetry via USB connection to ESP32
 * Uses strategy pattern for platform-agnostic connection management
 * Manages USB serial connection and data parsing
 */
public class LoRaReceiverService extends Service {
    private static final String CHANNEL_ID = "LoRaReceiverChannel";
    private static final int NOTIFICATION_ID = 2;
    
    public static final String ACTION_DATA_RECEIVED = "com.bravo.mobile.ACTION_LORA_DATA_RECEIVED";
    public static final String EXTRA_TELEMETRY = "telemetry_data";
    
    private IConnectionStrategy connectionStrategy;
    private final IBinder binder = new LocalBinder();
    private DataReceivedListener dataReceivedListener;

    public interface DataReceivedListener {
        void onTelemetryReceived(TelemetryData data);
        void onConnectionStateChanged(boolean connected);
    }

    public class LocalBinder extends Binder {
        public LoRaReceiverService getService() {
            return LoRaReceiverService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Create USB connection strategy using factory
        connectionStrategy = ConnectionStrategyFactory.createUSBStrategy(this);
        
        // Initialize the strategy
        connectionStrategy.initialize();
        
        // Set up listener for connection events
        connectionStrategy.setConnectionListener(new IConnectionListener() {
            @Override
            public void onConnectionStateChanged(boolean connected) {
                if (dataReceivedListener != null) {
                    dataReceivedListener.onConnectionStateChanged(connected);
                }
            }
            
            @Override
            public void onTelemetryReceived(TelemetryData data) {
                if (dataReceivedListener != null) {
                    dataReceivedListener.onTelemetryReceived(data);
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
        Notification notification = createNotification("LoRa Receiver Service Running");
        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Connect to USB device
     */
    public boolean connectToUSB() {
        if (connectionStrategy == null) {
            return false;
        }
        // Pass null for device identifier to auto-detect first available device
        return connectionStrategy.connect(null);
    }

    /**
     * Disconnect from USB device
     */
    public void disconnect() {
        if (connectionStrategy != null) {
            connectionStrategy.disconnect();
        }
    }

    /**
     * Check if connected to USB device
     */
    public boolean isConnected() {
        return connectionStrategy != null && connectionStrategy.isConnected();
    }

    /**
     * Set data received listener
     */
    public void setDataReceivedListener(DataReceivedListener listener) {
        this.dataReceivedListener = listener;
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
                "LoRa Receiver Service",
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
