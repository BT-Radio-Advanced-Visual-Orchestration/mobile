import Foundation

/**
 * Telemetry data model
 * Shared data structure for both Android and iOS
 */
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
    
    init(latitude: Double, longitude: Double, altitude: Double, speed: Double,
         rssi: Int, snr: Int, batteryLevel: Int, deviceId: String?) {
        self.latitude = latitude
        self.longitude = longitude
        self.altitude = altitude
        self.speed = speed
        self.rssi = rssi
        self.snr = snr
        self.batteryLevel = batteryLevel
        self.deviceId = deviceId
        self.timestamp = Date()
    }
}
