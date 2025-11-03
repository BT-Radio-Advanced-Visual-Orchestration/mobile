import Foundation

/**
 * Platform-agnostic listener protocol for connection events and data reception
 * Swift implementation matching the Java interface
 */
protocol IConnectionListener {
    
    /**
     * Called when connection state changes
     * @param connected true if connected, false if disconnected
     */
    func onConnectionStateChanged(connected: Bool)
    
    /**
     * Called when telemetry data is received
     * @param data Telemetry data received from the device
     */
    func onTelemetryReceived(data: TelemetryData)
    
    /**
     * Called when an error occurs
     * @param error Error message
     */
    func onError(error: String)
}
