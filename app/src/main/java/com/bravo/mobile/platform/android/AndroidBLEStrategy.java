package com.bravo.mobile.platform.android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.bravo.mobile.libs.TelemetryParser;
import com.bravo.mobile.models.LoRaPacket;
import com.bravo.mobile.models.TelemetryData;
import com.bravo.mobile.platform.interfaces.IConnectionListener;
import com.bravo.mobile.platform.interfaces.IConnectionStrategy;

/**
 * Android-specific implementation of BLE connection strategy
 * Handles Bluetooth Low Energy connections on Android devices
 */
public class AndroidBLEStrategy implements IConnectionStrategy {
    
    private final Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private IConnectionListener listener;
    
    // ESP32 Service and Characteristic UUIDs (placeholder - replace with actual UUIDs)
    private static final String SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    private static final String CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
    
    public AndroidBLEStrategy(Context context) {
        this.context = context;
    }
    
    @Override
    public boolean initialize() {
        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
        } catch (Exception e) {
            if (listener != null) {
                listener.onError("Failed to initialize BLE: " + e.getMessage());
            }
            return false;
        }
    }
    
    @Override
    public boolean connect(String deviceAddress) {
        if (bluetoothAdapter == null || deviceAddress == null) {
            if (listener != null) {
                listener.onError("Bluetooth adapter not initialized or invalid device address");
            }
            return false;
        }
        
        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            bluetoothGatt = device.connectGatt(context, false, gattCallback);
            return true;
        } catch (Exception e) {
            if (listener != null) {
                listener.onError("Failed to connect: " + e.getMessage());
            }
            return false;
        }
    }
    
    @Override
    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }
    
    @Override
    public boolean isConnected() {
        return bluetoothGatt != null;
    }
    
    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.BLUETOOTH_LOW_ENERGY;
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
     * BLE GATT callback for handling connection events and data
     */
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt.discoverServices();
                if (listener != null) {
                    listener.onConnectionStateChanged(true);
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (listener != null) {
                    listener.onConnectionStateChanged(false);
                }
            }
        }
        
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Enable notifications for the characteristic
                // This is where you would set up to receive data from ESP32
            }
        }
        
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, 
                                           BluetoothGattCharacteristic characteristic) {
            // Data received from ESP32
            byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                processReceivedData(data);
            }
        }
    };
    
    /**
     * Process received data from BLE
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
