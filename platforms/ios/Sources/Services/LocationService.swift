//
//  LocationService.swift
//  FypMatch iOS
//
//  Serviço de localização e geolocalização
//  Gerencia permissões e tracking de localização
//

import Foundation
import CoreLocation
import Combine

/// Serviço para gerenciamento de localização do app
@MainActor
class LocationService: NSObject, ObservableObject {
    
    // MARK: - Singleton
    static let shared = LocationService()
    
    // MARK: - Properties
    private let locationManager = CLLocationManager()
    
    @Published var currentLocation: CLLocation?
    @Published var authorizationStatus: CLAuthorizationStatus = .notDetermined
    @Published var locationError: String?
    @Published var isUpdatingLocation = false
    
    // MARK: - Computed Properties
    var hasLocationPermission: Bool {
        switch authorizationStatus {
        case .authorizedAlways, .authorizedWhenInUse:
            return true
        default:
            return false
        }
    }
    
    var canRequestLocation: Bool {
        return authorizationStatus == .notDetermined
    }
    
    // MARK: - Initialization
    override private init() {
        super.init()
        setupLocationManager()
    }
    
    // MARK: - Setup
    
    private func setupLocationManager() {
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = 100 // Update every 100 meters
        
        authorizationStatus = locationManager.authorizationStatus
    }
    
    // MARK: - Permission Management
    
    /// Request location permission
    func requestLocationPermission() {
        guard canRequestLocation else { return }
        locationManager.requestWhenInUseAuthorization()
    }
    
    /// Request always location permission
    func requestAlwaysAuthorization() {
        locationManager.requestAlwaysAuthorization()
    }
    
    /// Check if location services are enabled
    func checkLocationServicesEnabled() -> Bool {
        return CLLocationManager.locationServicesEnabled()
    }
    
    // MARK: - Location Tracking
    
    /// Start updating location
    func startUpdatingLocation() {
        guard hasLocationPermission else {
            locationError = "Permissão de localização não concedida"
            return
        }
        
        guard checkLocationServicesEnabled() else {
            locationError = "Serviços de localização desabilitados"
            return
        }
        
        isUpdatingLocation = true
        locationManager.startUpdatingLocation()
    }
    
    /// Stop updating location
    func stopUpdatingLocation() {
        isUpdatingLocation = false
        locationManager.stopUpdatingLocation()
    }
    
    /// Request single location update
    func requestLocation() async throws -> CLLocation {
        guard hasLocationPermission else {
            throw LocationError.permissionDenied
        }
        
        return try await withCheckedThrowingContinuation { continuation in
            var hasReturned = false
            
            locationManager.requestLocation()
            
            // Set timeout
            Task {
                try await Task.sleep(nanoseconds: 10_000_000_000) // 10 seconds
                if !hasReturned {
                    hasReturned = true
                    continuation.resume(throwing: LocationError.timeout)
                }
            }
            
            // Wait for location update (handled in delegate)
            Task { @MainActor in
                for await location in NotificationCenter.default.notifications(named: .didUpdateLocation).map({ $0.object as? CLLocation }) {
                    if let location = location, !hasReturned {
                        hasReturned = true
                        continuation.resume(returning: location)
                        break
                    }
                }
            }
        }
    }
    
    // MARK: - Distance Calculation
    
    /// Calculate distance between two locations in kilometers
    func calculateDistance(from location1: CLLocation, to location2: CLLocation) -> Double {
        let distanceInMeters = location1.distance(from: location2)
        return distanceInMeters / 1000.0 // Convert to kilometers
    }
    
    /// Calculate distance from current location to a coordinate
    func distanceToCoordinate(latitude: Double, longitude: Double) -> Double? {
        guard let currentLocation = currentLocation else { return nil }
        
        let targetLocation = CLLocation(latitude: latitude, longitude: longitude)
        return calculateDistance(from: currentLocation, to: targetLocation)
    }
    
    /// Check if location is within radius (in kilometers)
    func isLocationWithinRadius(latitude: Double, longitude: Double, radiusKm: Double) -> Bool {
        guard let distance = distanceToCoordinate(latitude: latitude, longitude: longitude) else {
            return false
        }
        return distance <= radiusKm
    }
    
    // MARK: - Geocoding
    
    /// Get address from coordinates (reverse geocoding)
    func getAddress(from location: CLLocation) async throws -> CLPlacemark {
        let geocoder = CLGeocoder()
        let placemarks = try await geocoder.reverseGeocodeLocation(location)
        
        guard let placemark = placemarks.first else {
            throw LocationError.geocodingFailed
        }
        
        return placemark
    }
    
    /// Get coordinates from address (forward geocoding)
    func getCoordinates(from address: String) async throws -> CLLocation {
        let geocoder = CLGeocoder()
        let placemarks = try await geocoder.geocodeAddressString(address)
        
        guard let placemark = placemarks.first,
              let location = placemark.location else {
            throw LocationError.geocodingFailed
        }
        
        return location
    }
    
    /// Get formatted address string
    func getFormattedAddress(from location: CLLocation) async throws -> String {
        let placemark = try await getAddress(from: location)
        
        var components: [String] = []
        
        if let city = placemark.locality {
            components.append(city)
        }
        
        if let state = placemark.administrativeArea {
            components.append(state)
        }
        
        if let country = placemark.country {
            components.append(country)
        }
        
        return components.joined(separator: ", ")
    }
    
    // MARK: - City/State Detection
    
    /// Get current city and state
    func getCurrentCityAndState() async throws -> (city: String?, state: String?) {
        guard let location = currentLocation else {
            throw LocationError.locationNotAvailable
        }
        
        let placemark = try await getAddress(from: location)
        return (city: placemark.locality, state: placemark.administrativeArea)
    }
    
    // MARK: - Background Location
    
    /// Enable background location updates
    func enableBackgroundLocationUpdates() {
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.pausesLocationUpdatesAutomatically = false
    }
    
    /// Disable background location updates
    func disableBackgroundLocationUpdates() {
        locationManager.allowsBackgroundLocationUpdates = false
        locationManager.pausesLocationUpdatesAutomatically = true
    }
}

// MARK: - CLLocationManagerDelegate

extension LocationService: CLLocationManagerDelegate {
    
    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        
        Task { @MainActor in
            self.currentLocation = location
            self.locationError = nil
            
            // Post notification for single location request
            NotificationCenter.default.post(name: .didUpdateLocation, object: location)
        }
    }
    
    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Task { @MainActor in
            self.locationError = error.localizedDescription
            self.isUpdatingLocation = false
        }
    }
    
    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            self.authorizationStatus = manager.authorizationStatus
            
            // Start location updates if permission granted
            if self.hasLocationPermission && self.isUpdatingLocation {
                manager.startUpdatingLocation()
            }
        }
    }
}

// MARK: - Errors

enum LocationError: Error, LocalizedError {
    case permissionDenied
    case locationNotAvailable
    case geocodingFailed
    case timeout
    case servicesDisabled
    
    var errorDescription: String? {
        switch self {
        case .permissionDenied:
            return "Permissão de localização negada"
        case .locationNotAvailable:
            return "Localização não disponível"
        case .geocodingFailed:
            return "Falha ao obter endereço"
        case .timeout:
            return "Tempo limite excedido ao obter localização"
        case .servicesDisabled:
            return "Serviços de localização desabilitados"
        }
    }
}

// MARK: - Notification Names

extension Notification.Name {
    static let didUpdateLocation = Notification.Name("didUpdateLocation")
    static let didChangeAuthorizationStatus = Notification.Name("didChangeAuthorizationStatus")
}

// MARK: - Location Extensions

extension CLLocation {
    /// Convert to coordinate string
    var coordinateString: String {
        return "\(coordinate.latitude), \(coordinate.longitude)"
    }
    
    /// Format coordinates for display
    var formattedCoordinates: String {
        let latDirection = coordinate.latitude >= 0 ? "N" : "S"
        let lonDirection = coordinate.longitude >= 0 ? "E" : "W"
        
        return String(format: "%.4f°%@ %.4f°%@",
                     abs(coordinate.latitude), latDirection,
                     abs(coordinate.longitude), lonDirection)
    }
}

extension CLLocationCoordinate2D {
    /// Calculate distance to another coordinate
    func distance(to coordinate: CLLocationCoordinate2D) -> Double {
        let location1 = CLLocation(latitude: self.latitude, longitude: self.longitude)
        let location2 = CLLocation(latitude: coordinate.latitude, longitude: coordinate.longitude)
        return location1.distance(from: location2) / 1000.0 // in kilometers
    }
}
