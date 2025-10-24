# BRAVO Mobile - Android Application

Android application for the B.R.A.V.O (Beacon-Relay-Asset-View-Orchestration) IoT system. This app connects to ESP32-based collar/dongle devices via USB or Bluetooth Low Energy (BLE), receives LoRa telemetry data, and displays GPS location information on an interactive map with offline support.

## Features

- **Dual Connection Support**: Connect to ESP32 devices via USB or BLE
- **LoRa Telemetry Reception**: Receive and parse GPS telemetry data transmitted via LoRa
- **Interactive Map Visualization**: Display real-time GPS tracking on OpenStreetMap
- **Offline Map Support**: View maps without internet connectivity using cached tiles
- **Real-time Telemetry Display**: View latitude, longitude, altitude, speed, signal strength, and battery level
- **Background Services**: Continuous data reception via foreground services
- **GPS Path Tracking**: Visual trail of device movement on the map

## Architecture

The project follows Android best practices with a modular structure:

```
app/src/main/java/com/bravo/mobile/
├── activities/          # UI Activities
│   ├── MainActivity.java
│   ├── MapActivity.java
│   └── SettingsActivity.java
├── services/           # Background Services
│   ├── BLEConnectionService.java
│   ├── LoRaReceiverService.java
│   └── UsbBroadcastReceiver.java
├── libs/              # Core Libraries
│   ├── LoRaReceiver.java
│   ├── TelemetryParser.java
│   └── MapVisualization.java
├── models/            # Data Models
│   ├── TelemetryData.java
│   └── LoRaPacket.java
└── utils/             # Utility Classes
    └── Constants.java
```

## Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 26+ (Android 8.0 Oreo)
- Target SDK 34 (Android 14)
- Java 8+
- Gradle 8.0+

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/beacon-relay-asset-view-orchestration/mobile.git
cd mobile
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to the cloned repository and select the project folder
4. Wait for Gradle sync to complete

### 3. Configure Google Maps API Key (Optional)

If you want to use Google Maps instead of OpenStreetMap:

1. Get an API key from [Google Cloud Console](https://console.cloud.google.com/)
2. Open `app/src/main/AndroidManifest.xml`
3. Replace `YOUR_GOOGLE_MAPS_API_KEY_HERE` with your actual API key

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_ACTUAL_API_KEY" />
```

### 4. Build the Project

```bash
./gradlew build
```

Or use Android Studio:
- Menu: Build → Make Project (Ctrl+F9 / Cmd+F9)

### 5. Run on Device or Emulator

```bash
./gradlew installDebug
```

Or use Android Studio:
- Menu: Run → Run 'app' (Shift+F10)

**Note**: For USB connections, you must use a physical Android device with OTG support.

## Usage

### Connecting via Bluetooth (BLE)

1. Launch the BRAVO Mobile app
2. Tap "Connect via Bluetooth"
3. Select your ESP32 device from the list (devices starting with "ESP32")
4. Wait for connection confirmation
5. Telemetry data will automatically appear on the main screen

### Connecting via USB

1. Connect ESP32 collar/dongle to your Android device using an OTG cable
2. Launch the BRAVO Mobile app
3. Tap "Connect via USB"
4. Grant USB permission when prompted
5. Telemetry data will automatically appear on the main screen

### Viewing GPS Map

1. After connecting to a device, tap "View GPS Map"
2. The map will display the current location from telemetry
3. A blue line shows the tracked path
4. Tap on the location marker to see detailed information

### Offline Maps

Offline maps are automatically enabled. The app will:
- Use cached tiles when available
- Download new tiles when online
- Continue showing cached areas when offline

To pre-download map areas:
1. While connected to WiFi, navigate to desired areas
2. Zoom in and pan around to cache tiles
3. These tiles will be available offline

## Permissions

The app requires the following permissions:

- **BLUETOOTH / BLUETOOTH_ADMIN**: For BLE connections
- **BLUETOOTH_SCAN / BLUETOOTH_CONNECT**: Required on Android 12+
- **ACCESS_FINE_LOCATION**: Required for BLE scanning and GPS display
- **USB_PERMISSION**: For USB serial communication
- **INTERNET**: For downloading map tiles
- **WRITE_EXTERNAL_STORAGE**: For caching offline maps (Android 11 and below)
- **FOREGROUND_SERVICE**: For background data reception

## Telemetry Data Format

The app supports two telemetry formats:

### JSON Format
```json
{
  "lat": 37.7749,
  "lon": -122.4194,
  "alt": 50.0,
  "spd": 10.5,
  "dev": "ESP32-001",
  "bat": 85
}
```

### Binary Format
- 4 bytes: Latitude (float)
- 4 bytes: Longitude (float)
- 4 bytes: Altitude (float)
- 4 bytes: Speed (float)
- 1 byte: Battery level (0-100)

## Development

### Adding New Features

1. **Models**: Add data structures to `models/`
2. **Services**: Add background tasks to `services/`
3. **Libraries**: Add reusable utilities to `libs/`
4. **Activities**: Add new screens to `activities/`

### Testing on Hardware

For best results, test with:
- ESP32 DevKit with LoRa module (SX1276/SX1278)
- GPS module connected to ESP32
- Android device with BLE 4.0+ support
- USB OTG cable for USB testing

### Debugging

Enable verbose logging:
```bash
adb logcat -s BLEConnectionService LoRaReceiverService MainActivity MapActivity
```

## Troubleshooting

### BLE Connection Issues
- Ensure Bluetooth is enabled
- Grant location permissions (required for BLE scanning)
- Make sure ESP32 is advertising
- Check device name matches expected format

### USB Connection Issues
- Use a quality OTG cable
- Verify USB host support on your device
- Check USB drivers are loaded
- Grant USB permissions when prompted

### Map Not Loading
- Check internet connection for first load
- Verify storage permissions for offline caching
- Clear app cache if tiles appear corrupted
- Zoom out and pan to refresh tiles

### No Telemetry Data
- Verify ESP32 is transmitting data
- Check serial baud rate matches (115200)
- Ensure LoRa frequency and settings match
- Review packet format matches expected structure

## Dependencies

- **AndroidX**: Core Android libraries
- **Material Design**: UI components
- **Google Play Services**: Maps and location
- **OSMDroid**: Offline map support
- **Nordic BLE Library**: BLE communication
- **USB Serial for Android**: USB serial communication
- **Gson**: JSON parsing

## License

This project is part of the B.R.A.V.O IoT system. Please refer to the main repository for licensing information.

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
- [ ] Multi-device tracking
- [ ] Historical telemetry data export
- [ ] Advanced map features (heatmaps, geofencing)
- [ ] Remote device configuration
- [ ] Data analytics dashboard
- [ ] Battery optimization improvements

## Credits

Developed for the B.R.A.V.O IoT system project.
