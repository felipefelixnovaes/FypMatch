plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
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
            // API Key do Gemini para desenvolvimento
            buildConfigField("String", "GEMINI_API_KEY", "\"AIzaSyAsUX8dj3_OKuHWQlEsBEGa0d3mWFqat2E\"")
        }
        
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("boolean", "FIREBASE_DEBUG", "false")
            // API Key do Gemini para produção
            buildConfigField("String", "GEMINI_API_KEY", "\"AIzaSyAsUX8dj3_OKuHWQlEsBEGa0d3mWFqat2E\"")
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
    
    // Hilt - Dependency Injection - VERSÃO MAIS RECENTE (KSP)
    implementation("com.google.dagger:hilt-android:2.54")
    ksp("com.google.dagger:hilt-compiler:2.54")
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
    implementation("com.google.firebase:firebase-appcheck")
    implementation("com.google.firebase:firebase-appcheck-debug")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    
    // Google Services - VERSÕES MAIS RECENTES
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    
    // Google Play Billing
    implementation("com.android.billingclient:billing:7.1.1")
    implementation("com.android.billingclient:billing-ktx:7.1.1")
    
    // Coil para carregamento de imagens - VERSÃO MAIS RECENTE
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // Testing - Suíte Completa
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("app.cash.turbine:turbine:1.1.0") // Para testar Flows
    testImplementation("com.google.truth:truth:1.4.4") // Assertions mais legíveis
    testImplementation("org.robolectric:robolectric:4.13") // Testes Android sem emulador
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.test.ext:junit:1.2.1")
    
    // Hilt Testing (KSP)
    testImplementation("com.google.dagger:hilt-android-testing:2.54")
    kspTest("com.google.dagger:hilt-compiler:2.54")
    
    // Android Testing
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.54")
    kspAndroidTest("com.google.dagger:hilt-compiler:2.54")
    
    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Google Sign-In - API Clássica (Popup nativo)
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    
    // Google Sign-In - Nova API Credentials (Fallback)
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

// 🧪 ===============================================
// 🧪 CONFIGURAÇÕES DE TESTE - FypMatch
// 🧪 ===============================================

// Configuração adicional para testes
tasks.withType<Test> {
    useJUnitPlatform()
    
    // Configurações de memória
    maxHeapSize = "2g"
    jvmArgs("-XX:MaxMetaspaceSize=512m")
    
    // Configurações de relatório
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
    
    // Configurações de logging
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
    }
    
    // Executar em paralelo para melhor performance
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    
    // Configurações específicas do FypMatch
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    
    // Variáveis de ambiente para testes
    environment("FYPMATCH_TEST_MODE", "true")
    environment("FIREBASE_TEST_MODE", "true")
}

// Tarefa para executar todos os testes do FypMatch
tasks.register("runFypMatchTests") {
    group = "FypMatch Tests"
    description = "Executa todos os testes do FypMatch"
    
    dependsOn("test")
    
    doLast {
        println("""
        
        🧪 ===============================================
        🧪 EXECUÇÃO DE TESTES FINALIZADA - FypMatch
        🧪 ===============================================
        
        📊 Relatórios disponíveis em:
        📄 app/build/reports/tests/testDebugUnitTest/index.html
        📊 app/build/test-results/testDebugUnitTest/
        
        🎯 Para executar testes específicos:
        ./gradlew test --tests "AuthRepositoryTest"
        ./gradlew test --tests "LocationViewModelTest"
        ./gradlew test --tests "ProfileViewModelTest"
        
        """.trimIndent())
    }
}

// Tarefa para mostrar informações sobre os testes
tasks.register("testInfo") {
    group = "FypMatch Tests"
    description = "Mostra informações sobre os testes disponíveis"
    
    doLast {
        println("""
        
        🧪 ===============================================
        🧪 INFORMAÇÕES DE TESTES - FypMatch
        🧪 ===============================================
        
        📋 Categorias de Testes Disponíveis:
        
        🔐 Autenticação:
           - AuthRepositoryTest
           - LoginViewModelTest
           - GoogleSignInTest
           - EmailSignInTest
           - PhoneSignInTest
        
        👤 Perfil:
           - ProfileViewModelTest
           - ProfileRepositoryTest
           - UserRepositoryTest
        
        📍 Localização:
           - LocationViewModelTest
           - LocationRepositoryTest
           - LocationServiceTest
           - LocationManagerTest
           - DistanceCalculationTest
        
        🎯 Comandos Úteis:
        
        ./gradlew runFypMatchTests              # Todos os testes
        ./gradlew test                          # Testes padrão
        ./gradlew cleanTest                     # Limpar e testar
        
        ./gradlew test --tests "AuthRepositoryTest"     # Teste específico
        ./gradlew test --tests "*Location*"            # Testes de localização
        ./gradlew test --continuous                     # Modo contínuo
        
        📊 Relatórios:
        - HTML: app/build/reports/tests/testDebugUnitTest/index.html
        - XML:  app/build/test-results/testDebugUnitTest/
        - Logs: app/build/reports/tests/testDebugUnitTest/classes/
        
        """.trimIndent())
    }
}