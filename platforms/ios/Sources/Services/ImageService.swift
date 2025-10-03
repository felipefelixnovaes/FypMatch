//
//  ImageService.swift
//  FypMatch iOS
//
//  Serviço para gerenciamento de imagens
//  Upload, download e processamento de imagens
//

import Foundation
import UIKit
import FirebaseStorage
import Combine

/// Serviço para gerenciamento de imagens do app
@MainActor
class ImageService: ObservableObject {
    
    // MARK: - Singleton
    static let shared = ImageService()
    
    // MARK: - Properties
    private let storage = Storage.storage()
    private let maxImageSize: CGFloat = 1920
    private let compressionQuality: CGFloat = 0.8
    private let thumbnailSize: CGFloat = 400
    
    @Published var isUploading = false
    @Published var uploadProgress: Double = 0.0
    @Published var uploadError: String?
    
    // MARK: - Initialization
    private init() {}
    
    // MARK: - Image Processing
    
    /// Redimensiona imagem mantendo proporção
    func resizeImage(_ image: UIImage, maxSize: CGFloat = 1920) -> UIImage? {
        let size = image.size
        let widthRatio = maxSize / size.width
        let heightRatio = maxSize / size.height
        let ratio = min(widthRatio, heightRatio)
        
        guard ratio < 1.0 else { return image }
        
        let newSize = CGSize(width: size.width * ratio, height: size.height * ratio)
        let renderer = UIGraphicsImageRenderer(size: newSize)
        
        return renderer.image { _ in
            image.draw(in: CGRect(origin: .zero, size: newSize))
        }
    }
    
    /// Comprime imagem para JPEG
    func compressImage(_ image: UIImage, quality: CGFloat = 0.8) -> Data? {
        return image.jpegData(compressionQuality: quality)
    }
    
    /// Cria thumbnail de imagem
    func createThumbnail(_ image: UIImage, size: CGFloat = 400) -> UIImage? {
        return resizeImage(image, maxSize: size)
    }
    
    /// Processa imagem para upload (redimensiona e comprime)
    func processImageForUpload(_ image: UIImage) -> Data? {
        guard let resized = resizeImage(image, maxSize: maxImageSize) else { return nil }
        return compressImage(resized, quality: compressionQuality)
    }
    
    // MARK: - Upload
    
    /// Upload de imagem de perfil
    func uploadProfileImage(_ image: UIImage, userId: String) async throws -> String {
        guard let imageData = processImageForUpload(image) else {
            throw ImageServiceError.processingFailed
        }
        
        let path = "users/\(userId)/photos/\(UUID().uuidString).jpg"
        return try await uploadImage(imageData, path: path)
    }
    
    /// Upload de imagem de chat
    func uploadChatImage(_ image: UIImage, conversationId: String) async throws -> String {
        guard let imageData = processImageForUpload(image) else {
            throw ImageServiceError.processingFailed
        }
        
        let path = "chats/\(conversationId)/images/\(UUID().uuidString).jpg"
        return try await uploadImage(imageData, path: path)
    }
    
    /// Upload genérico de imagem
    private func uploadImage(_ imageData: Data, path: String) async throws -> String {
        await MainActor.run {
            self.isUploading = true
            self.uploadProgress = 0.0
            self.uploadError = nil
        }
        
        let storageRef = storage.reference().child(path)
        
        do {
            // Create upload task with progress
            let metadata = StorageMetadata()
            metadata.contentType = "image/jpeg"
            
            _ = try await storageRef.putDataAsync(imageData, metadata: metadata)
            
            // Get download URL
            let downloadURL = try await storageRef.downloadURL()
            
            await MainActor.run {
                self.isUploading = false
                self.uploadProgress = 1.0
            }
            
            return downloadURL.absoluteString
            
        } catch {
            await MainActor.run {
                self.isUploading = false
                self.uploadProgress = 0.0
                self.uploadError = error.localizedDescription
            }
            throw ImageServiceError.uploadFailed(error.localizedDescription)
        }
    }
    
    /// Upload de múltiplas imagens
    func uploadMultipleImages(_ images: [UIImage], userId: String) async throws -> [String] {
        var urls: [String] = []
        
        for (index, image) in images.enumerated() {
            let url = try await uploadProfileImage(image, userId: userId)
            urls.append(url)
            
            await MainActor.run {
                self.uploadProgress = Double(index + 1) / Double(images.count)
            }
        }
        
        return urls
    }
    
    // MARK: - Download
    
    /// Download de imagem do Firebase Storage
    func downloadImage(from url: String) async throws -> UIImage {
        guard let imageURL = URL(string: url) else {
            throw ImageServiceError.invalidURL
        }
        
        let (data, _) = try await URLSession.shared.data(from: imageURL)
        
        guard let image = UIImage(data: data) else {
            throw ImageServiceError.invalidImageData
        }
        
        return image
    }
    
    // MARK: - Delete
    
    /// Deleta imagem do Storage
    func deleteImage(url: String) async throws {
        guard let storageRef = Storage.storage().reference(forURL: url) else {
            throw ImageServiceError.invalidURL
        }
        
        try await storageRef.delete()
    }
    
    /// Deleta múltiplas imagens
    func deleteMultipleImages(urls: [String]) async throws {
        for url in urls {
            try await deleteImage(url: url)
        }
    }
    
    // MARK: - Cache
    
    private var imageCache: [String: UIImage] = [:]
    
    /// Get cached image or download
    func getCachedImage(url: String) async throws -> UIImage {
        if let cachedImage = imageCache[url] {
            return cachedImage
        }
        
        let image = try await downloadImage(from: url)
        imageCache[url] = image
        return image
    }
    
    /// Clear image cache
    func clearCache() {
        imageCache.removeAll()
    }
}

// MARK: - Errors

enum ImageServiceError: Error, LocalizedError {
    case processingFailed
    case uploadFailed(String)
    case invalidURL
    case invalidImageData
    case deleteFailed
    
    var errorDescription: String? {
        switch self {
        case .processingFailed:
            return "Falha ao processar imagem"
        case .uploadFailed(let message):
            return "Falha no upload: \(message)"
        case .invalidURL:
            return "URL inválida"
        case .invalidImageData:
            return "Dados de imagem inválidos"
        case .deleteFailed:
            return "Falha ao deletar imagem"
        }
    }
}

// MARK: - Image Extensions

extension UIImage {
    /// Converte UIImage para Data
    var jpegData: Data? {
        return self.jpegData(compressionQuality: 0.8)
    }
    
    /// Converte UIImage para Data PNG
    var pngData: Data? {
        return self.pngData()
    }
    
    /// Redimensiona imagem
    func resize(to size: CGSize) -> UIImage? {
        let renderer = UIGraphicsImageRenderer(size: size)
        return renderer.image { _ in
            self.draw(in: CGRect(origin: .zero, size: size))
        }
    }
    
    /// Cria imagem circular
    func makeCircular() -> UIImage? {
        let size = CGSize(width: self.size.width, height: self.size.height)
        let renderer = UIGraphicsImageRenderer(size: size)
        
        return renderer.image { context in
            let rect = CGRect(origin: .zero, size: size)
            let path = UIBezierPath(ovalIn: rect)
            path.addClip()
            self.draw(in: rect)
        }
    }
}
