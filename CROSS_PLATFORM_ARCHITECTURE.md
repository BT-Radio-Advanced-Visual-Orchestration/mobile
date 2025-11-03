# Cross-Platform Architecture

This document explains the cross-platform architecture of the B.R.A.V.O. Mobile application, which supports both Android and iOS devices using the Strategy Pattern.

## Overview

The B.R.A.V.O. Mobile application has been refactored to support both Android and iOS platforms while maximizing code reuse and maintaining platform-specific optimizations. The architecture uses the **Strategy Pattern** to abstract platform-specific implementations behind common interfaces.

## Design Principles

1. **Platform Abstraction**: Common interfaces define the contract for all platform-specific implementations
2. **Strategy Pattern**: Platform-specific strategies are selected at runtime based on the host platform
3. **Separation of Concerns**: Business logic is separated from platform-specific code
4. **Code Reuse**: Maximum reuse of data models and parsing logic across platforms
5. **Maintainability**: Changes to one platform don't affect the other

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
│         (Activities/ViewControllers, Services)               │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ↓
┌─────────────────────────────────────────────────────────────┐
│                Platform-Agnostic Layer                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │      IConnectionStrategy Interface                    │   │
│  │   - initialize()                                      │   │
│  │   - connect(deviceIdentifier)                        │   │
│  │   - disconnect()                                     │   │
│  │   - isConnected()                                    │   │
│  │   - getConnectionType()                              │   │
│  │   - setConnectionListener()                          │   │
│  │   - cleanup()                                        │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │      IConnectionListener Interface                    │   │
│  │   - onConnectionStateChanged(connected)              │   │
│  │   - onTelemetryReceived(data)                        │   │
│  │   - onError(error)                                   │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │      ConnectionStrategyFactory                        │   │
│  │   - createBLEStrategy(context)                       │   │
│  │   - createUSBStrategy(context)                       │   │
│  │   - createStrategy(context, type)                    │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │      Shared Data Models                               │   │
│  │   - TelemetryData                                    │   │
│  │   - LoRaPacket                                       │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────┬───────────────────────────────────────┘
                      │
        ┌─────────────┴─────────────┐
        │                           │
        ↓                           ↓
┌───────────────────┐     ┌───────────────────┐
│  Android Layer    │     │    iOS Layer      │
├───────────────────┤     ├───────────────────┤
│ AndroidBLEStrategy│     │ IOSBLEStrategy    │
│ AndroidUSBStrategy│     │ IOSUSBStrategy    │
│                   │     │                   │
│ Uses:             │     │ Uses:             │
│ - BluetoothGatt   │     │ - CoreBluetooth   │
│ - UsbSerial       │     │ - ExternalAccess. │
└───────────────────┘     └───────────────────┘
```

## Platform-Agnostic Interfaces

### IConnectionStrategy

The core interface for connection management across platforms.

**Java (Android):**
```java
public interface IConnectionStrategy {
    boolean initialize();
    boolean connect(String deviceIdentifier);
    void disconnect();
    boolean isConnected();
    ConnectionType getConnectionType();
    void setConnectionListener(IConnectionListener listener);
    void cleanup();
    
    enum ConnectionType {
        BLUETOOTH_LOW_ENERGY,
        USB_SERIAL
    }
}
```

**Swift (iOS):**
```swift
protocol IConnectionStrategy {
    func initialize() -> Bool
    func connect(deviceIdentifier: String?) -> Bool
    func disconnect()
    func isConnected() -> Bool
    func getConnectionType() -> ConnectionType
    func setConnectionListener(listener: IConnectionListener)
    func cleanup()
}

enum ConnectionType {
    case bluetoothLowEnergy
    case usbSerial
}
```

### IConnectionListener

Callback interface for connection events and data reception.

**Java (Android):**
```java
public interface IConnectionListener {
    void onConnectionStateChanged(boolean connected);
    void onTelemetryReceived(TelemetryData data);
    void onError(String error);
}
```

**Swift (iOS):**
```swift
protocol IConnectionListener {
    func onConnectionStateChanged(connected: Bool)
    func onTelemetryReceived(data: TelemetryData)
    func onError(error: String)
}
```

## Platform-Specific Implementations

### Android Implementation

**AndroidBLEStrategy** (`app/src/main/java/com/bravo/mobile/platform/android/AndroidBLEStrategy.java`)
- Uses Android's `BluetoothAdapter`, `BluetoothGatt`, and `BluetoothGattCallback`
- Implements BLE device discovery and connection
- Handles GATT service and characteristic discovery
- Processes incoming telemetry data

**AndroidUSBStrategy** (`app/src/main/java/com/bravo/mobile/platform/android/AndroidUSBStrategy.java`)
- Uses `UsbManager` and the `usb-serial-for-android` library
- Implements USB serial communication
- Manages read threads for continuous data reception
- Handles USB device attachment/detachment

### iOS Implementation

**IOSBLEStrategy** (`ios/BRAVOMobile/Platform/IOSBLEStrategy.swift`)
- Uses `CoreBluetooth` framework (`CBCentralManager`, `CBPeripheral`)
- Implements BLE scanning and connection
- Handles service and characteristic discovery
- Processes notification-based data reception

**IOSUSBStrategy** (`ios/BRAVOMobile/Platform/IOSUSBStrategy.swift`)
- Uses `ExternalAccessory` framework (`EAAccessory`, `EASession`)
- Implements MFi accessory protocol support
- Manages input/output streams for data transfer
- Handles accessory connection/disconnection events

## Strategy Pattern Implementation

### Factory Pattern

The `ConnectionStrategyFactory` creates the appropriate strategy based on platform and connection type:

**Android:**
```java
public class ConnectionStrategyFactory {
    public static IConnectionStrategy createBLEStrategy(Context context) {
        PlatformType platform = PlatformDetector.detectPlatform();
        
        switch (platform) {
            case ANDROID:
                return new AndroidBLEStrategy(context);
            case IOS:
                throw new UnsupportedOperationException("iOS not supported on Android");
            default:
                throw new UnsupportedOperationException("Unsupported platform");
        }
    }
    
    public static IConnectionStrategy createUSBStrategy(Context context) {
        PlatformType platform = PlatformDetector.detectPlatform();
        
        switch (platform) {
            case ANDROID:
                return new AndroidUSBStrategy(context);
            case IOS:
                throw new UnsupportedOperationException("iOS not supported on Android");
            default:
                throw new UnsupportedOperationException("Unsupported platform");
        }
    }
}
```

### Platform Detection

The `PlatformDetector` class identifies the current platform:

```java
public class PlatformDetector {
    public static PlatformType detectPlatform() {
        // Check for Android
        try {
            Class.forName("android.os.Build");
            return PlatformType.ANDROID;
        } catch (ClassNotFoundException e) {
            // Not Android
        }
        
        // Check for iOS
        String osName = System.getProperty("os.name", "");
        if (osName.toLowerCase().contains("ios")) {
            return PlatformType.IOS;
        }
        
        return PlatformType.UNKNOWN;
    }
}
```

## Shared Data Models

Both platforms use the same data structures for telemetry information:

**Android (Java):**
```java
public class TelemetryData {
    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;
    private int rssi;
    private int snr;
    private int batteryLevel;
    private String deviceId;
    private Date timestamp;
    
    // Constructor, getters, setters...
}
```

**iOS (Swift):**
```swift
struct TelemetryData {
    let latitude: Double
    let longitude: Double
    let altitude: Double
    let speed: Double
    let rssi: Int
    let snr: Int
    let batteryLevel: Int
    let deviceId: String?
    let timestamp: Date
}
```

## Usage Example

### Android

```java
// In BLEConnectionService.java
@Override
public void onCreate() {
    super.onCreate();
    
    // Create BLE strategy using factory
    connectionStrategy = ConnectionStrategyFactory.createBLEStrategy(this);
    
    // Initialize
    connectionStrategy.initialize();
    
    // Set up listener
    connectionStrategy.setConnectionListener(new IConnectionListener() {
        @Override
        public void onConnectionStateChanged(boolean connected) {
            // Handle connection state change
        }
        
        @Override
        public void onTelemetryReceived(TelemetryData data) {
            // Handle received telemetry data
        }
        
        @Override
        public void onError(String error) {
            // Handle errors
        }
    });
}

// Connect to device
connectionStrategy.connect(deviceAddress);
```

### iOS

```swift
// In ConnectionService.swift
class ConnectionService {
    private var connectionStrategy: IConnectionStrategy?
    
    func setupBLEConnection() {
        // Create BLE strategy
        connectionStrategy = IOSBLEStrategy()
        
        // Initialize
        _ = connectionStrategy?.initialize()
        
        // Set up listener
        connectionStrategy?.setConnectionListener(listener: self)
    }
    
    func connect() {
        connectionStrategy?.connect(deviceIdentifier: nil)
    }
}

extension ConnectionService: IConnectionListener {
    func onConnectionStateChanged(connected: Bool) {
        // Handle connection state change
    }
    
    func onTelemetryReceived(data: TelemetryData) {
        // Handle received telemetry data
    }
    
    func onError(error: String) {
        // Handle errors
    }
}
```

## Benefits of This Architecture

1. **Code Reuse**: Common interfaces and data models are shared
2. **Maintainability**: Platform-specific code is isolated
3. **Testability**: Easy to mock strategies for unit testing
4. **Extensibility**: New platforms can be added by implementing the interfaces
5. **Flexibility**: Easy to swap connection strategies at runtime
6. **Clarity**: Clear separation between platform-agnostic and platform-specific code

## Platform-Specific Considerations

### Android
- Requires runtime permissions for Bluetooth and Location (Android 6.0+)
- USB connections require OTG support
- Foreground services needed for continuous background operation
- Notification required for foreground services

### iOS
- Requires usage descriptions in Info.plist for Bluetooth
- USB connections limited to MFi certified accessories
- Background modes must be declared for BLE in background
- External Accessory protocols must be registered in Info.plist

## Testing Strategy

### Unit Tests
- Test each strategy implementation independently
- Mock the platform-specific APIs
- Verify correct implementation of interfaces

### Integration Tests
- Test factory pattern creation
- Verify strategy selection based on platform
- Test listener callbacks

### Platform Tests
- Android: Instrumentation tests with Espresso
- iOS: XCTest for UI and unit tests
- Both: Test with real hardware when possible

## Future Enhancements

1. **Shared Business Logic**: Consider using Kotlin Multiplatform or similar for shared code
2. **Additional Platforms**: Extend to support other platforms (Windows, macOS, Web)
3. **Plugin Architecture**: Allow third-party connection strategies
4. **Configuration Management**: Platform-agnostic configuration system
5. **Unified Testing**: Cross-platform testing framework

## Contributing

When contributing to the cross-platform architecture:

1. Maintain interface consistency across platforms
2. Document platform-specific limitations
3. Keep platform-agnostic code truly platform-agnostic
4. Update both Android and iOS implementations when changing interfaces
5. Test on both platforms before submitting PRs

## References

- [Android Bluetooth Low Energy](https://developer.android.com/guide/topics/connectivity/bluetooth/ble-overview)
- [Android USB Host](https://developer.android.com/guide/topics/connectivity/usb/host)
- [iOS CoreBluetooth](https://developer.apple.com/documentation/corebluetooth)
- [iOS External Accessory](https://developer.apple.com/documentation/externalaccessory)
- [Strategy Pattern](https://refactoring.guru/design-patterns/strategy)
