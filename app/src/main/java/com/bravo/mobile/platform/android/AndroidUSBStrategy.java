package com.bravo.mobile.platform.android;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.bravo.mobile.libs.TelemetryParser;
import com.bravo.mobile.models.LoRaPacket;
import com.bravo.mobile.models.TelemetryData;
import com.bravo.mobile.platform.interfaces.IConnectionListener;
import com.bravo.mobile.platform.interfaces.IConnectionStrategy;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

/**
 * Android-specific implementation of USB connection strategy
 * Handles USB serial connections on Android devices
 */
public class AndroidUSBStrategy implements IConnectionStrategy {
    
    private final Context context;
    private UsbManager usbManager;
    private UsbSerialPort serialPort;
    private IConnectionListener listener;
    private Thread receiverThread;
    private volatile boolean isReceiving = false;
    
    private static final int BAUD_RATE = 115200;
    
    public AndroidUSBStrategy(Context context) {
        this.context = context;
    }
    
    @Override
    public boolean initialize() {
        try {
            usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            return usbManager != null;
        } catch (Exception e) {
            if (listener != null) {
                listener.onError("Failed to initialize USB: " + e.getMessage());
            }
            return false;
        }
    }
    
    @Override
    public boolean connect(String deviceIdentifier) {
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber()
                .findAllDrivers(usbManager);
        
        if (availableDrivers.isEmpty()) {
            if (listener != null) {
                listener.onError("No USB devices found");
            }
            return false;
        }
        
        // Get the first available driver (ESP32)
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = usbManager.openDevice(driver.getDevice());
        
        if (connection == null) {
            if (listener != null) {
                listener.onError("Failed to open USB device connection");
            }
            return false;
        }
        
        try {
            serialPort = driver.getPorts().get(0);
            serialPort.open(connection);
            serialPort.setParameters(BAUD_RATE, 8, UsbSerialPort.STOPBITS_1, 
                                    UsbSerialPort.PARITY_NONE);
            
            // Start receiving data
            startReceiving();
            
            if (listener != null) {
                listener.onConnectionStateChanged(true);
            }
            
            return true;
        } catch (IOException e) {
            if (listener != null) {
                listener.onError("Failed to connect to USB device: " + e.getMessage());
            }
            return false;
        }
    }
    
    @Override
    public void disconnect() {
        isReceiving = false;
        
        if (receiverThread != null) {
            receiverThread.interrupt();
            receiverThread = null;
        }
        
        if (serialPort != null) {
            try {
                serialPort.close();
            } catch (IOException e) {
                // Ignore close errors
            }
            serialPort = null;
        }
        
        if (listener != null) {
            listener.onConnectionStateChanged(false);
        }
    }
    
    @Override
    public boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }
    
    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.USB_SERIAL;
    }
    
    @Override
    public void setConnectionListener(IConnectionListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void cleanup() {
        disconnect();
        listener = null;
    }
    
    /**
     * Start receiving data from USB serial port
     */
    private void startReceiving() {
        if (serialPort == null) {
            return;
        }
        
        isReceiving = true;
        receiverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[256];
                while (isReceiving && serialPort != null && serialPort.isOpen()) {
                    try {
                        int bytesRead = serialPort.read(buffer, 1000);
                        if (bytesRead > 0) {
                            byte[] data = new byte[bytesRead];
                            System.arraycopy(buffer, 0, data, 0, bytesRead);
                            processReceivedData(data);
                        }
                    } catch (IOException e) {
                        if (isReceiving && listener != null) {
                            listener.onError("Error reading USB data: " + e.getMessage());
                        }
                        break;
                    }
                }
            }
        });
        receiverThread.start();
    }
    
    /**
     * Process received data from USB
     */
    private void processReceivedData(byte[] data) {
        try {
            LoRaPacket packet = new LoRaPacket(data, 0, 0);
            TelemetryData telemetry = TelemetryParser.parse(packet);
            
            if (telemetry != null && listener != null) {
                listener.onTelemetryReceived(telemetry);
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onError("Failed to process data: " + e.getMessage());
            }
        }
    }
}
