# B.R.A.V.O. Mobile - iOS Application

iOS application for the B.R.A.V.O. (Beacon-Relay-Asset-View-Orchestration) IoT system. This app connects to ESP32-based relay devices via Bluetooth Low Energy (BLE) or USB (via MFi accessories) to access LoRa telemetry data, and displays GPS location information on an interactive map.

**Important**: This mobile app does NOT receive LoRa radio signals directly. LoRa information is only accessible on the phone through:
- **ESP32 Relay Device**: The phone connects to an ESP32 device (via BLE or USB/Lightning) which receives LoRa transmissions from collars/dongles
- **API/Dashboard**: Alternative access through a web API or dashboard interface (future feature)

## Features

- **Dual Connection Support**: Connect to ESP32 relay devices via BLE or USB (MFi accessories)
- **LoRa Telemetry Reception**: Access GPS telemetry data transmitted via LoRa through the ESP32 relay
- **Interactive Map Visualization**: Display real-time GPS tracking on OpenStreetMap
- **Offline Map Support**: View maps without internet connectivity using cached tiles
- **Real-time Telemetry Display**: View latitude, longitude, altitude, speed, signal strength, and battery level
- **Background Services**: Continuous data reception via background tasks
- **GPS Path Tracking**: Visual trail of device movement on the map

## Architecture Overview

The B.R.A.V.O. system uses a **relay architecture** for LoRa communication:

```
[GPS Collar/Dongle] --LoRa Radio--> [ESP32 Relay] --BLE/USB--> [iPhone/iPad]
                                          ^
                                          |
                                     Receives LoRa
                                     transmissions
```

The mobile phone **cannot receive LoRa radio directly**. Instead:
1. GPS collar/dongle devices transmit location data via LoRa radio
2. An ESP32 relay device receives these LoRa transmissions
3. The mobile app connects to the ESP32 relay (via BLE or USB/Lightning)
4. Telemetry data is forwarded from the relay to the phone

## Platform Abstraction

The iOS app implements the same platform-agnostic interfaces as the Android app, using the **Strategy Pattern** for connection management:

```
ios/BRAVOMobile/
├── Platform/                    # Platform-specific implementations
│   ├── IConnectionStrategy.swift      # Connection strategy protocol
│   ├── IConnectionListener.swift      # Connection listener protocol
│   ├── IOSBLEStrategy.swift           # iOS BLE implementation
│   └── IOSUSBStrategy.swift           # iOS USB implementation (MFi)
├── Models/                      # Data models
│   └── TelemetryData.swift            # Telemetry data structure
├── Services/                    # Background services
└── Views/                       # UI Views
```

## Prerequisites

- Xcode 14.0 or later
- iOS 14.0+ deployment target
- Swift 5.0+
- CocoaPods or Swift Package Manager

## Map Implementation

Both iOS and Android use **OpenStreetMap** for consistent cross-platform map visualization:
- iOS implementation can use libraries like [Mapbox Maps SDK](https://github.com/mapbox/mapbox-maps-ios) or custom OpenStreetMap tile rendering
- Provides offline map support with cached tiles
- Consistent map appearance across all devices
- No dependency on platform-specific mapping services

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/beacon-relay-asset-view-orchestration/mobile.git
cd mobile/ios
```

### 2. Open in Xcode

1. Launch Xcode
2. Open `BRAVOMobile.xcodeproj` or `BRAVOMobile.xcworkspace` (if using CocoaPods)
3. Wait for dependencies to resolve

### 3. Configure Signing

1. Select the BRAVOMobile target in Xcode
2. Go to "Signing & Capabilities"
3. Select your development team
4. Xcode will automatically manage provisioning profiles

### 4. Configure Info.plist

For USB accessories (MFi), add the supported external accessory protocols:

```xml
<key>UISupportedExternalAccessoryProtocols</key>
<array>
    <string>com.bravo.esp32</string>
</array>
```

For Bluetooth, ensure you have the usage descriptions:

```xml
<key>NSBluetoothAlwaysUsageDescription</key>
<string>BRAVO Mobile needs Bluetooth to connect to ESP32 relay devices</string>
<key>NSBluetoothPeripheralUsageDescription</key>
<string>BRAVO Mobile needs Bluetooth to connect to ESP32 relay devices</string>
```

### 5. Build and Run

- Select a target device or simulator
- Press Cmd+R to build and run
- **Note**: BLE and USB features require a physical iOS device

## Usage

### Connecting via Bluetooth (BLE)

1. Launch the B.R.A.V.O. Mobile app
2. Tap "Connect via Bluetooth"
3. Select your ESP32 relay device from the list
4. Wait for connection confirmation
5. Telemetry data from LoRa collars will automatically appear on the main screen

**Note**: The ESP32 relay must be within BLE range and actively receiving LoRa transmissions.

### Connecting via USB/Lightning

1. Connect ESP32 relay device to your iOS device using a Lightning to USB adapter
2. The device must be MFi certified or using an MFi accessory protocol
3. Launch the BRAVO Mobile app
4. The app should automatically detect the accessory
5. Telemetry data from LoRa collars will automatically appear on the main screen

**Note**: iOS USB support is limited to MFi accessories. Non-certified accessories may not work.

## Permissions

The app requires the following permissions:

- **Bluetooth**: For BLE connections to ESP32 relay devices
- **Location** (when in use): For displaying GPS data on the map
- **Background Modes**: For continuous data reception
  - Bluetooth-central
  - Location updates (if needed)

## Connection Strategy Pattern

The iOS app uses the same strategy pattern as Android:

```swift
// BLE Connection
let bleStrategy = IOSBLEStrategy()
bleStrategy.initialize()
bleStrategy.setConnectionListener(listener)
bleStrategy.connect(deviceIdentifier: nil)

// USB Connection
let usbStrategy = IOSUSBStrategy()
usbStrategy.initialize()
usbStrategy.setConnectionListener(listener)
usbStrategy.connect(deviceIdentifier: nil)
```

## Development

### Project Structure

- **Platform/**: Platform-specific implementations of connection strategies
- **Models/**: Shared data models (TelemetryData, etc.)
- **Services/**: Background services and managers
- **Views/**: SwiftUI or UIKit views for the user interface

### Testing on Hardware

For best results, test with:
- ESP32 DevKit with LoRa module (SX1276/SX1278) configured as a relay
- GPS collar/dongle with LoRa transmitter
- iOS device with BLE 4.0+ support
- Lightning to USB adapter for USB testing
- MFi certified USB accessory (for USB connections)

## Troubleshooting

### BLE Connection Issues
- Ensure Bluetooth is enabled in iOS Settings
- Grant Bluetooth permissions when prompted
- Make sure ESP32 is advertising and within range
- Check device name matches expected format

### USB Connection Issues
- Verify the accessory is MFi certified
- Check that the protocol string is registered in Info.plist
- Ensure the Lightning to USB adapter is Apple certified
- Grant accessory permissions when prompted

### Map Not Loading
- Check internet connection for first load
- Verify location permissions are granted
- Clear app cache if tiles appear corrupted

### No Telemetry Data
- Verify ESP32 relay is receiving LoRa transmissions
- Verify GPS collar/dongle is transmitting via LoRa
- Ensure LoRa frequency and settings match between collar and relay
- Review packet format matches expected structure
- Check that collar/dongle is within LoRa range of the relay

## Cross-Platform Compatibility

This iOS app shares the same architectural principles as the Android app:

- **Shared Interfaces**: Both platforms implement the same connection strategy interfaces
- **Consistent Data Models**: TelemetryData structure is identical across platforms
- **Reusable Logic**: Telemetry parsing and processing logic can be shared
- **Platform-Specific Implementations**: Each platform provides its own BLE and USB strategies

## License

This project is part of the B.R.A.V.O. IoT system. Please refer to the main repository for licensing information.

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## Support

For issues, questions, or contributions, please visit:
- GitHub Issues: https://github.com/beacon-relay-asset-view-orchestration/mobile/issues
- Main Project: https://github.com/beacon-relay-asset-view-orchestration

## Roadmap

Future enhancements:
- [ ] SwiftUI interface implementation
- [ ] Multi-device tracking
- [ ] Historical telemetry data export
- [ ] Advanced map features (heatmaps, geofencing)
- [ ] Remote device configuration
- [ ] Data analytics dashboard
- [ ] Battery optimization improvements
- [ ] Widget support for iOS 14+

## Credits

Developed for the B.R.A.V.O. IoT system project.
