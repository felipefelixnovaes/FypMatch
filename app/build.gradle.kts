plugins {

    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    // id("kotlin-kapt")
    // id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.ideiassertiva.FypMatch"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "com.ideiassertiva.FypMatch"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // Firebase Analytics Debug Mode - Para debug no dispositivo
        manifestPlaceholders["enableFirebaseAnalyticsDebugging"] = "true"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../fypmatch-release.keystore")
            storePassword = "fypmatch2024"
            keyAlias = "fypmatch"
            keyPassword = "fypmatch2024"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            // Habilitar Firebase Analytics Debug View para todos os builds debug
            buildConfigField("boolean", "FIREBASE_DEBUG", "true")
        }
        
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("boolean", "FIREBASE_DEBUG", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM - Versão mais recente 🚀
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    
    // Core Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    
    // Material Design 3
    implementation("androidx.compose.material3:material3")
    
    // Activity Compose - VERSÃO MAIS RECENTE
    implementation("androidx.activity:activity-compose:1.9.3")
    
    // Navigation Compose - VERSÃO MAIS RECENTE
    implementation("androidx.navigation:navigation-compose:2.8.5")
    
    // ViewModel Compose - VERSÃO MAIS RECENTE
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    
    // Hilt - Dependency Injection - VERSÃO MAIS RECENTE
    implementation("com.google.dagger:hilt-android:2.54")
    kapt("com.google.dagger:hilt-compiler:2.54")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    
    // Core Android - VERSÃO MAIS RECENTE
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    
    // Material Components
    implementation("com.google.android.material:material:1.12.0")
    
    // Firebase (usando BOM 33.7.0 acima)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-config-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-appcheck-debug")
    
    // Google Services - VERSÕES MAIS RECENTES
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    
    // Google Play Billing
    implementation("com.android.billingclient:billing:7.1.1")
    implementation("com.android.billingclient:billing-ktx:7.1.1")
    
    // Coil para carregamento de imagens - VERSÃO MAIS RECENTE
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    
    // Android Testing
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Google Sign-In - Nova API Credentials (MAIS RECENTE)
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    
    // Google Play Services - VERSÕES MAIS RECENTES
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    
    // Networking - VERSÕES MAIS RECENTES
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Permissions - VERSÃO MAIS RECENTE
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")
    
    // Date Picker
    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0")
    
    // Lottie Animations - VERSÃO MAIS RECENTE
    implementation("com.airbnb.android:lottie-compose:6.6.0")
    
    // Google AdMob - VERSÃO MAIS RECENTE
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    
    // Gemini AI - VERSÃO MAIS RECENTE
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
}