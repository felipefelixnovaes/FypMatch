package com.ideiassertiva.FypMatch.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.ideiassertiva.FypMatch.data.repository.*
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
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideUserRepository(firestore: FirebaseFirestore, auth: FirebaseAuth): UserRepository {
        return UserRepository(firestore, auth)
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
    fun provideFirebaseChatRepository(firestore: FirebaseFirestore): FirebaseChatRepository {
        return FirebaseChatRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideDiscoveryRepository(firestore: FirebaseFirestore): DiscoveryRepository {
        return DiscoveryRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideAffiliateRepository(firestore: FirebaseFirestore): AffiliateRepository {
        return AffiliateRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(@ApplicationContext context: Context): AuthRepository {
        return AuthRepository(context)
    }

    @Provides
    @Singleton
    fun provideWaitlistRepository(firestore: FirebaseFirestore): WaitlistRepository {
        return WaitlistRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideQuestionnaireRepository(): QuestionnaireRepository {
        return QuestionnaireRepository()
    }

    @Provides
    @Singleton
    fun providePhase4AIRepository(): Phase4AIRepository {
        return Phase4AIRepository()
    }
}
