import Foundation
import ExternalAccessory

/**
 * iOS-specific implementation of USB connection strategy
 * Handles USB connections on iOS devices using External Accessory framework
 * Note: iOS has limited USB host support, primarily through MFi accessories
 */
class IOSUSBStrategy: NSObject, IConnectionStrategy, EAAccessoryDelegate, StreamDelegate {
    
    private var accessory: EAAccessory?
    private var session: EASession?
    private var inputStream: InputStream?
    private var outputStream: OutputStream?
    private var listener: IConnectionListener?
    private var readBuffer = [UInt8](repeating: 0, count: 256)
    
    // Protocol string for ESP32 accessory (must be registered in Info.plist)
    private let protocolString = "com.bravo.esp32"
    
    // MARK: - IConnectionStrategy Implementation
    
    func initialize() -> Bool {
        // Register for accessory notifications
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(accessoryConnected),
            name: .EAAccessoryDidConnect,
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(accessoryDisconnected),
            name: .EAAccessoryDidDisconnect,
            object: nil
        )
        
        EAAccessoryManager.shared().registerForLocalNotifications()
        return true
    }
    
    func connect(deviceIdentifier: String?) -> Bool {
        let connectedAccessories = EAAccessoryManager.shared().connectedAccessories
        
        // Find the first accessory that supports our protocol
        for accessory in connectedAccessories {
            if accessory.protocolStrings.contains(protocolString) {
                return connectToAccessory(accessory)
            }
        }
        
        listener?.onError(error: "No compatible USB accessory found")
        return false
    }
    
    private func connectToAccessory(_ accessory: EAAccessory) -> Bool {
        self.accessory = accessory
        session = EASession(accessory: accessory, forProtocol: protocolString)
        
        guard let session = session else {
            listener?.onError(error: "Failed to create accessory session")
            return false
        }
        
        inputStream = session.inputStream
        outputStream = session.outputStream
        
        inputStream?.delegate = self
        outputStream?.delegate = self
        
        inputStream?.schedule(in: .current, forMode: .default)
        outputStream?.schedule(in: .current, forMode: .default)
        
        inputStream?.open()
        outputStream?.open()
        
        listener?.onConnectionStateChanged(connected: true)
        return true
    }
    
    func disconnect() {
        inputStream?.close()
        outputStream?.close()
        
        inputStream?.remove(from: .current, forMode: .default)
        outputStream?.remove(from: .current, forMode: .default)
        
        inputStream = nil
        outputStream = nil
        session = nil
        accessory = nil
        
        listener?.onConnectionStateChanged(connected: false)
    }
    
    func isConnected() -> Bool {
        return session != nil && inputStream?.streamStatus == .open
    }
    
    func getConnectionType() -> ConnectionType {
        return .usbSerial
    }
    
    func setConnectionListener(listener: IConnectionListener) {
        self.listener = listener
    }
    
    func cleanup() {
        disconnect()
        NotificationCenter.default.removeObserver(self)
        listener = nil
    }
    
    // MARK: - Accessory Notifications
    
    @objc private func accessoryConnected(notification: Notification) {
        if let accessory = notification.userInfo?[EAAccessoryKey] as? EAAccessory {
            if accessory.protocolStrings.contains(protocolString) {
                _ = connectToAccessory(accessory)
            }
        }
    }
    
    @objc private func accessoryDisconnected(notification: Notification) {
        if let accessory = notification.userInfo?[EAAccessoryKey] as? EAAccessory {
            if accessory == self.accessory {
                disconnect()
            }
        }
    }
    
    // MARK: - StreamDelegate
    
    func stream(_ aStream: Stream, handle eventCode: Stream.Event) {
        switch eventCode {
        case .hasBytesAvailable:
            guard let inputStream = aStream as? InputStream else { return }
            
            let bytesRead = inputStream.read(&readBuffer, maxLength: readBuffer.count)
            if bytesRead > 0 {
                let data = Data(readBuffer[0..<bytesRead])
                processReceivedData(data: data)
            }
            
        case .errorOccurred:
            listener?.onError(error: "Stream error occurred")
            
        case .endEncountered:
            disconnect()
            
        default:
            break
        }
    }
    
    // MARK: - Data Processing
    
    private func processReceivedData(data: Data) {
        // Convert Data to byte array and parse as LoRa packet
        let bytes = [UInt8](data)
        
        // Parse telemetry data (implementation would call shared parser)
        // For now, this is a placeholder
        // let telemetry = TelemetryParser.parse(bytes)
        // listener?.onTelemetryReceived(data: telemetry)
    }
}
