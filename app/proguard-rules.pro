# ═══════════════════════════════════════════════════════════════════════════
# FypMatch — ProGuard / R8 rules para release
# ═══════════════════════════════════════════════════════════════════════════

# Preserva line numbers para stack traces legíveis no Play Console
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-renamesourcefileattribute SourceFile

# ─── Modelos de dados (serializados no Firestore) ──────────────────────────
# data classes que vão/voltam do Firestore precisam manter campos e construtores
-keep class com.ideiassertiva.FypMatch.model.** { *; }
-keep class com.ideiassertiva.FypMatch.models.** { *; }

# Mantém construtores sem argumento exigidos pelo Firestore (toObject)
-keepclassmembers class com.ideiassertiva.FypMatch.model.** {
    <init>();
    <fields>;
}
-keepclassmembers class com.ideiassertiva.FypMatch.models.** {
    <init>();
    <fields>;
}

# ─── Firebase / Firestore ──────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
# Firestore usa reflection para mapear documentos -> objetos
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.PropertyName <fields>;
}
-keep class * extends com.google.firebase.firestore.** { *; }

# ─── Hilt / Dagger ─────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }
-keepclasseswithmembers class * {
    @dagger.* <methods>;
}
-dontwarn dagger.hilt.**

# ─── Kotlin Coroutines ─────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ─── Kotlin metadata / reflection ──────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.coroutines.Continuation

# ─── Jetpack Compose ───────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ─── Coil (carregamento de imagens) ────────────────────────────────────────
-keep class coil.** { *; }
-dontwarn coil.**

# ─── Google Sign-In ────────────────────────────────────────────────────────
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }

# ─── Enums (usados em quando/when e Firestore) ─────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ─── Parcelable ────────────────────────────────────────────────────────────
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ─── Modelos de questionário e ML (mantém integridade dos algoritmos) ──────
-keep class com.ideiassertiva.FypMatch.data.repository.** { *; }
