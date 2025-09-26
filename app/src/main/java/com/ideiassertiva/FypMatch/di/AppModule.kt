package com.ideiassertiva.FypMatch.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.ideiassertiva.FypMatch.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository {
        return UserRepository()
    }
    
    @Provides
    @Singleton
    fun provideAccessControlRepository(): AccessControlRepository {
        return AccessControlRepository()
    }
    
    @Provides
    @Singleton
    fun provideAICounselorRepository(): AICounselorRepository {
        return AICounselorRepository()
    }
    
    @Provides
    @Singleton
    fun provideAdsRepository(): AdsRepository {
        return AdsRepository()
    }
    
    @Provides
    @Singleton
    fun provideAccessCodeRepository(): AccessCodeRepository {
        return AccessCodeRepository()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideChatRepository(firestore: FirebaseFirestore): ChatRepository {
        // For now, return the existing mock implementation
        // In a future iteration, we can create an interface and switch implementations
        return ChatRepository()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseChatRepository(firestore: FirebaseFirestore): FirebaseChatRepository {
        return FirebaseChatRepository(firestore)
    }
    
    @Provides
    @Singleton
    fun provideDiscoveryRepository(): DiscoveryRepository {
        return DiscoveryRepository()
    }
} 
