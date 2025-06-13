package com.ideiassertiva.FypMatch

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FypMatchApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        
        // Configurar Analytics
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
        
        // Configurar Firestore com configurações otimizadas
        // Especificar explicitamente o banco "(default)"
        val firestore = FirebaseFirestore.getInstance("(default)")
        val firestoreSettings = FirebaseFirestoreSettings.Builder()
            .build()
        
        firestore.firestoreSettings = firestoreSettings
        
        // Log de inicialização
        println("🚀 FypMatch Application iniciado com Firebase configurado")
    }
}
