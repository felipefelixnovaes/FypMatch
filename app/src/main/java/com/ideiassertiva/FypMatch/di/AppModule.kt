package com.ideiassertiva.FypMatch.di

import android.content.Context
import com.ideiassertiva.FypMatch.data.repository.*
import com.ideiassertiva.FypMatch.util.AnalyticsManager
import com.ideiassertiva.FypMatch.util.LocationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }
    
    @Provides
    @Singleton
    fun provideAnalyticsManager(@ApplicationContext context: Context): AnalyticsManager {
        return AnalyticsManager(context)
    }
    
    @Provides
    @Singleton
    fun provideLocationManager(
        @ApplicationContext context: Context,
        analyticsManager: AnalyticsManager
    ): LocationManager {
        return LocationManager(context, analyticsManager)
    }
    
    // === ARQUITETURA OTIMIZADA PLANO SPARK ===
    
    @Provides
    @Singleton
    fun provideUserRepository(analyticsManager: AnalyticsManager): UserRepository {
        return UserRepository(analyticsManager)
    }
    
    @Provides
    @Singleton
    fun provideLocationRepository(analyticsManager: AnalyticsManager): LocationRepository {
        return LocationRepository(analyticsManager)
    }
    
    @Provides
    @Singleton
    fun providePhotoRepository(analyticsManager: AnalyticsManager): PhotoRepository {
        return PhotoRepository(analyticsManager)
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context,
        analyticsManager: AnalyticsManager,
        userRepository: UserRepository,
        locationRepository: LocationRepository,
        accessControlRepository: AccessControlRepository
    ): AuthRepository {
        return AuthRepository(context, analyticsManager, userRepository, locationRepository, accessControlRepository)
    }
    
    @Provides
    @Singleton
    fun provideChatRepository(analyticsManager: AnalyticsManager): ChatRepository {
        return ChatRepository(analyticsManager)
    }
    
    // === REPOSITÓRIOS EXISTENTES ===
    
    @Provides
    @Singleton
    fun provideAccessControlRepository(): AccessControlRepository {
        return AccessControlRepository()
    }
    
    @Provides
    @Singleton
    fun provideAICounselorRepository(geminiRepository: GeminiRepository): AICounselorRepository {
        return AICounselorRepository(geminiRepository)
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
    fun provideGeminiRepository(analyticsManager: AnalyticsManager): GeminiRepository {
        return GeminiRepository(analyticsManager)
    }
    
    @Provides
    @Singleton
    fun provideDiscoveryRepository(): DiscoveryRepository {
        return DiscoveryRepository()
    }
    
    @Provides
    @Singleton
    fun provideRewardedAdsRepository(
        @ApplicationContext context: Context
    ): RewardedAdsRepository {
        return RewardedAdsRepository(context)
    }
    
    @Provides
    @Singleton
    fun provideGooglePlayBillingRepository(
        @ApplicationContext context: Context,
        analyticsManager: AnalyticsManager
    ): GooglePlayBillingRepository {
        return GooglePlayBillingRepository(context, analyticsManager)
    }
} 
