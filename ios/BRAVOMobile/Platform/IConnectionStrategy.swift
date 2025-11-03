import Foundation

/**
 * Platform-agnostic protocol for device connection strategies
 * Swift implementation matching the Java interface
 */
protocol IConnectionStrategy {
    
    /**
     * Initialize the connection strategy
     * @return true if initialization was successful
     */
    func initialize() -> Bool
    
    /**
     * Connect to a device
     * @param deviceIdentifier Device identifier (address for BLE, path for USB)
     * @return true if connection initiated successfully
     */
    func connect(deviceIdentifier: String?) -> Bool
    
    /**
     * Disconnect from the current device
     */
    func disconnect()
    
    /**
     * Check if currently connected to a device
     * @return true if connected
     */
    func isConnected() -> Bool
    
    /**
     * Get the connection type
     * @return ConnectionType enum value
     */
    func getConnectionType() -> ConnectionType
    
    /**
     * Set listener for connection events
     * @param listener Listener to receive connection events
     */
    func setConnectionListener(listener: IConnectionListener)
    
    /**
     * Clean up resources
     */
    func cleanup()
}

/**
 * Connection type enumeration
 */
enum ConnectionType {
    case bluetoothLowEnergy
    case usbSerial
}
