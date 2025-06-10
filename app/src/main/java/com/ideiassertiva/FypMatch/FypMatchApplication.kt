package com.ideiassertiva.FypMatch

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.ideiassertiva.FypMatch.data.repository.AdMobRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FypMatchApplication : Application() {
    
    @Inject
    lateinit var adMobRepository: AdMobRepository
    
    override fun onCreate() {
        super.onCreate()
        
        // Configurar Firestore para evitar problemas offline
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        firestore.firestoreSettings = settings
        
        // Inicializa AdMob em background
        CoroutineScope(Dispatchers.IO).launch {
            adMobRepository.initializeAdMob()
        }
    }
}
