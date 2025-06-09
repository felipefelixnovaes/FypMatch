package com.example.matchreal.di

import com.example.matchreal.data.repository.*

object AppModule {
    
    fun provideUserRepository(): UserRepository {
        return UserRepository()
    }
    
    fun provideAccessControlRepository(): AccessControlRepository {
        return AccessControlRepository()
    }
    
    fun provideAICounselorRepository(): AICounselorRepository {
        return AICounselorRepository()
    }
    
    fun provideAdsRepository(): AdsRepository {
        return AdsRepository()
    }
    
    fun provideAccessCodeRepository(): AccessCodeRepository {
        return AccessCodeRepository()
    }
} 