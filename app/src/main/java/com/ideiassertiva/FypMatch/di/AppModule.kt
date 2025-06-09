package com.ideiassertiva.FypMatch.di

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
    fun provideChatRepository(): ChatRepository {
        return ChatRepository()
    }
    
    @Provides
    @Singleton
    fun provideDiscoveryRepository(): DiscoveryRepository {
        return DiscoveryRepository()
    }
} 
