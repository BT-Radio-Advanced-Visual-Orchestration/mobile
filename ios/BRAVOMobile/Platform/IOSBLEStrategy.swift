import Foundation
import CoreBluetooth

/**
 * iOS-specific implementation of BLE connection strategy
 * Handles Bluetooth Low Energy connections on iOS devices using CoreBluetooth
 */
class IOSBLEStrategy: NSObject, IConnectionStrategy, CBCentralManagerDelegate, CBPeripheralDelegate {
    
    private var centralManager: CBCentralManager?
    private var peripheral: CBPeripheral?
    private var listener: IConnectionListener?
    private var isInitialized = false
    
    // ESP32 Service and Characteristic UUIDs (placeholder - replace with actual UUIDs)
    private let serviceUUID = CBUUID(string: "4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private let characteristicUUID = CBUUID(string: "beb5483e-36e1-4688-b7f5-ea07361b26a8")
    
    // MARK: - IConnectionStrategy Implementation
    
    func initialize() -> Bool {
        centralManager = CBCentralManager(delegate: self, queue: nil)
        // Wait for central manager to be ready
        isInitialized = true
        return true
    }
    
    func connect(deviceIdentifier: String?) -> Bool {
        guard let centralManager = centralManager,
              centralManager.state == .poweredOn else {
            listener?.onError(error: "Bluetooth is not powered on")
            return false
        }
        
        // Start scanning for peripherals with the ESP32 service UUID
        centralManager.scanForPeripherals(withServices: [serviceUUID], options: nil)
        return true
    }
    
    func disconnect() {
        if let peripheral = peripheral {
            centralManager?.cancelPeripheralConnection(peripheral)
        }
        self.peripheral = nil
    }
    
    func isConnected() -> Bool {
        return peripheral?.state == .connected
    }
    
    func getConnectionType() -> ConnectionType {
        return .bluetoothLowEnergy
    }
    
    func setConnectionListener(listener: IConnectionListener) {
        self.listener = listener
    }
    
    func cleanup() {
        disconnect()
        centralManager?.stopScan()
        listener = nil
    }
    
    // MARK: - CBCentralManagerDelegate
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .poweredOn:
            // Bluetooth is ready
            break
        case .poweredOff:
            listener?.onError(error: "Bluetooth is powered off")
        case .unauthorized:
            listener?.onError(error: "Bluetooth access is not authorized")
        case .unsupported:
            listener?.onError(error: "Bluetooth is not supported on this device")
        default:
            break
        }
    }
    
    func centralManager(_ central: CBCentralManager, 
                       didDiscover peripheral: CBPeripheral,
                       advertisementData: [String : Any],
                       rssi RSSI: NSNumber) {
        // Found a peripheral matching our service UUID
        self.peripheral = peripheral
        peripheral.delegate = self
        central.stopScan()
        central.connect(peripheral, options: nil)
    }
    
    func centralManager(_ central: CBCentralManager, 
                       didConnect peripheral: CBPeripheral) {
        listener?.onConnectionStateChanged(connected: true)
        peripheral.discoverServices([serviceUUID])
    }
    
    func centralManager(_ central: CBCentralManager, 
                       didDisconnectPeripheral peripheral: CBPeripheral,
                       error: Error?) {
        listener?.onConnectionStateChanged(connected: false)
        if let error = error {
            listener?.onError(error: "Disconnected: \(error.localizedDescription)")
        }
    }
    
    // MARK: - CBPeripheralDelegate
    
    func peripheral(_ peripheral: CBPeripheral, 
                   didDiscoverServices error: Error?) {
        guard error == nil else {
            listener?.onError(error: "Service discovery error: \(error!.localizedDescription)")
            return
        }
        
        if let services = peripheral.services {
            for service in services {
                if service.uuid == serviceUUID {
                    peripheral.discoverCharacteristics([characteristicUUID], for: service)
                }
            }
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral,
                   didDiscoverCharacteristicsFor service: CBService,
                   error: Error?) {
        guard error == nil else {
            listener?.onError(error: "Characteristic discovery error: \(error!.localizedDescription)")
            return
        }
        
        if let characteristics = service.characteristics {
            for characteristic in characteristics {
                if characteristic.uuid == characteristicUUID {
                    // Enable notifications for this characteristic
                    peripheral.setNotifyValue(true, for: characteristic)
                }
            }
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral,
                   didUpdateValueFor characteristic: CBCharacteristic,
                   error: Error?) {
        guard error == nil,
              let data = characteristic.value else {
            return
        }
        
        // Process received data
        processReceivedData(data: data)
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
