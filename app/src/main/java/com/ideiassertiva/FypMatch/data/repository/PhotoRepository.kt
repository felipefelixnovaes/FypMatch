package com.ideiassertiva.FypMatch.data.repository

import com.ideiassertiva.FypMatch.util.AnalyticsManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Coleção para metadados de fotos no Firestore
    private val photosCollection = firestore.collection("photos")
    
    /**
     * SOLUÇÃO PARA PLANO SPARK (SEM FIREBASE STORAGE)
     * 
     * Estratégias implementadas:
     * 1. Usar fotos do Google Profile (gratuitas)
     * 2. URLs externas temporárias (Imgur, etc.)
     * 3. Preparação para upgrade futuro com Cloud Functions
     * 4. Metadados no Firestore (incluído no plano)
     */
    
    // === 📸 GERENCIAMENTO DE FOTOS ===
    
    suspend fun savePhotoMetadata(
        userId: String,
        photoUrl: String,
        source: PhotoSource,
        isMainPhoto: Boolean = false
    ): Result<PhotoMetadata> {
        return try {
            val photoMetadata = PhotoMetadata(
                id = generatePhotoId(),
                userId = userId,
                url = photoUrl,
                source = source,
                isMainPhoto = isMainPhoto,
                uploadedAt = System.currentTimeMillis(),
                isActive = true
            )
            
            // Salvar metadados no Firestore
            photosCollection.document(photoMetadata.id).set(photoMetadata).await()
            
            // Analytics
            analyticsManager.logCustomCrash("photo_metadata_saved", mapOf(
                "user_id" to userId,
                "photo_source" to source.name,
                "is_main_photo" to isMainPhoto.toString()
            ))
            
            Result.success(photoMetadata)
        } catch (e: Exception) {
            analyticsManager.logError(e, "photo_metadata_save_error")
            Result.failure(e)
        }
    }
    
    suspend fun getUserPhotos(userId: String): Result<List<PhotoMetadata>> {
        return try {
            val snapshot = photosCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val photos = snapshot.documents.mapNotNull { 
                it.toObject(PhotoMetadata::class.java) 
            }.sortedWith(
                compareByDescending<PhotoMetadata> { it.isMainPhoto }
                    .thenBy { it.uploadedAt }
            )
            
            Result.success(photos)
        } catch (e: Exception) {
            analyticsManager.logError(e, "get_user_photos_error")
            Result.failure(e)
        }
    }
    
    suspend fun setMainPhoto(userId: String, photoId: String): Result<Unit> {
        return try {
            // Remover main de todas as fotos do usuário
            val userPhotos = getUserPhotos(userId).getOrThrow()
            userPhotos.forEach { photo ->
                if (photo.isMainPhoto) {
                    photosCollection.document(photo.id)
                        .update("isMainPhoto", false)
                        .await()
                }
            }
            
            // Definir nova foto principal
            photosCollection.document(photoId)
                .update("isMainPhoto", true)
                .await()
            
            // Analytics
            analyticsManager.logCustomCrash("main_photo_changed", mapOf(
                "user_id" to userId,
                "photo_id" to photoId
            ))
            
            Result.success(Unit)
        } catch (e: Exception) {
            analyticsManager.logError(e, "set_main_photo_error")
            Result.failure(e)
        }
    }
    
    suspend fun deletePhoto(userId: String, photoId: String): Result<Unit> {
        return try {
            // Marcar como inativa (soft delete)
            photosCollection.document(photoId)
                .update("isActive", false)
                .await()
            
            // Analytics
            analyticsManager.logCustomCrash("photo_deleted", mapOf(
                "user_id" to userId,
                "photo_id" to photoId
            ))
            
            Result.success(Unit)
        } catch (e: Exception) {
            analyticsManager.logError(e, "delete_photo_error")
            Result.failure(e)
        }
    }
    
    // === 🔗 INTEGRAÇÃO COM SERVIÇOS EXTERNOS ===
    
    /**
     * Preparação para integração com serviços de upload gratuitos
     * Imgur, Cloudinary free tier, etc.
     */
    suspend fun uploadToExternalService(
        userId: String,
        imageData: ByteArray,
        service: ExternalPhotoService = ExternalPhotoService.IMGUR
    ): Result<String> {
        return try {
            // TODO: Implementar upload para serviços externos
            // Por enquanto, retornar URL placeholder
            val placeholderUrl = when (service) {
                ExternalPhotoService.IMGUR -> "https://i.imgur.com/placeholder_${userId}.jpg"
                ExternalPhotoService.CLOUDINARY -> "https://res.cloudinary.com/demo/image/upload/placeholder_${userId}.jpg"
                ExternalPhotoService.GOOGLE_DRIVE -> "https://drive.google.com/uc?id=placeholder_${userId}"
            }
            
            // Analytics
            analyticsManager.logCustomCrash("external_upload_simulated", mapOf(
                "user_id" to userId,
                "service" to service.name,
                "placeholder_url" to placeholderUrl
            ))
            
            Result.success(placeholderUrl)
        } catch (e: Exception) {
            analyticsManager.logError(e, "external_upload_error")
            Result.failure(e)
        }
    }
    
    // === 🎨 VALIDAÇÃO E OTIMIZAÇÃO ===
    
    fun validateImageUrl(url: String): Boolean {
        return try {
            val validExtensions = listOf(".jpg", ".jpeg", ".png", ".webp")
            val validDomains = listOf(
                "googleusercontent.com",    // Google Profile
                "imgur.com",               // Imgur
                "cloudinary.com",          // Cloudinary
                "drive.google.com"         // Google Drive
            )
            
            validExtensions.any { url.lowercase().contains(it) } ||
            validDomains.any { url.contains(it) }
        } catch (e: Exception) {
            false
        }
    }
    
    fun getPhotoDisplayUrl(photo: PhotoMetadata, size: PhotoSize = PhotoSize.MEDIUM): String {
        return when (photo.source) {
            PhotoSource.GOOGLE_PROFILE -> {
                // Google Profile URLs podem ser redimensionadas
                when (size) {
                    PhotoSize.THUMBNAIL -> photo.url.replace("=s96-c", "=s150-c")
                    PhotoSize.MEDIUM -> photo.url.replace("=s96-c", "=s300-c")
                    PhotoSize.LARGE -> photo.url.replace("=s96-c", "=s600-c")
                }
            }
            PhotoSource.EXTERNAL_URL -> photo.url
            PhotoSource.PLACEHOLDER -> generatePlaceholderUrl(photo.userId, size)
        }
    }
    
    private fun generatePlaceholderUrl(userId: String, size: PhotoSize): String {
        val sizeParam = when (size) {
            PhotoSize.THUMBNAIL -> "150x150"
            PhotoSize.MEDIUM -> "300x300"
            PhotoSize.LARGE -> "600x600"
        }
        
        // Usar serviço de placeholder gratuito
        return "https://via.placeholder.com/$sizeParam/4A90E2/FFFFFF?text=FypMatch"
    }
    
    // === 🔧 UTILIDADES ===
    
    private fun generatePhotoId(): String {
        return "photo_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    suspend fun getPhotoStats(userId: String): PhotoStats {
        return try {
            val photos = getUserPhotos(userId).getOrNull() ?: emptyList()
            
            PhotoStats(
                totalPhotos = photos.size,
                mainPhotoSet = photos.any { it.isMainPhoto },
                sourceBreakdown = photos.groupBy { it.source }.mapValues { it.value.size },
                hasGooglePhoto = photos.any { it.source == PhotoSource.GOOGLE_PROFILE }
            )
        } catch (e: Exception) {
            PhotoStats()
        }
    }
}

// === 📋 MODELOS DE DADOS ===

data class PhotoMetadata(
    val id: String = "",
    val userId: String = "",
    val url: String = "",
    val source: PhotoSource = PhotoSource.EXTERNAL_URL,
    val isMainPhoto: Boolean = false,
    val uploadedAt: Long = 0L,
    val isActive: Boolean = true,
    val description: String = "",
    val tags: List<String> = emptyList()
)

enum class PhotoSource {
    GOOGLE_PROFILE,    // Foto do Google (gratuita)
    EXTERNAL_URL,      // URL externa (Imgur, etc.)
    PLACEHOLDER        // Placeholder gerado
}

enum class ExternalPhotoService {
    IMGUR,            // Imgur API (gratuita)
    CLOUDINARY,       // Cloudinary free tier
    GOOGLE_DRIVE      // Google Drive público
}

enum class PhotoSize {
    THUMBNAIL,        // 150x150
    MEDIUM,          // 300x300  
    LARGE            // 600x600
}

data class PhotoStats(
    val totalPhotos: Int = 0,
    val mainPhotoSet: Boolean = false,
    val sourceBreakdown: Map<PhotoSource, Int> = emptyMap(),
    val hasGooglePhoto: Boolean = false
) {
    val canAddMore: Boolean
        get() = totalPhotos < 6 // Limite de 6 fotos
        
    val needsMainPhoto: Boolean
        get() = totalPhotos > 0 && !mainPhotoSet
} 