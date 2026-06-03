package com.ideiassertiva.FypMatch.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * FypMatch Color Tokens — Alinhado ao design brief (Rosa #E91E63 / Roxo #9C27B0)
 * Superfícies tintadas com matiz primário (~2% chroma) — nunca cinza puro
 *
 * Primary seed: #E91E63 — rosa acolhedor (dating app, arquétipo O Amante)
 * Secondary seed: #9C27B0 — roxo complementar
 */
object FypColors {
    // =============================================
    // Light Theme
    // =============================================

    // Primary — rosa (romance/acolhimento)
    val Primary = Color(0xFFE91E63)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFFFD9E2)
    val OnPrimaryContainer = Color(0xFF3E001D)

    // Secondary — roxo (profundidade/confiança)
    val Secondary = Color(0xFF9C27B0)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFF3D9FF)
    val OnSecondaryContainer = Color(0xFF35004A)

    // Tertiary — coral quente (energia/calor)
    val Tertiary = Color(0xFFFF6D3F)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFFFFDBD0)
    val OnTertiaryContainer = Color(0xFF3A0A00)

    // Error
    val Error = Color(0xFFDC2626)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF410002)

    // Success
    val Success = Color(0xFF16A34A)
    val OnSuccess = Color(0xFFFFFFFF)
    val SuccessContainer = Color(0xFFDCFCE7)
    val OnSuccessContainer = Color(0xFF052E16)

    // Semantic tokens para ações do discovery
    val Like = Color(0xFF4CAF50)
    val OnLike = Color(0xFFFFFFFF)
    val Pass = Color(0xFFF44336)
    val OnPass = Color(0xFFFFFFFF)
    val SuperLike = Color(0xFF2196F3)
    val OnSuperLike = Color(0xFFFFFFFF)
    val Gold = Color(0xFFFFD700)
    val OnGold = Color(0xFF3A2E00)

    // Surface — tinted neutrals (matiz rosa ~2% chroma, NUNCA cinza puro)
    val Surface = Color(0xFFFFFBFA)
    val OnSurface = Color(0xFF1F1A1B)
    val SurfaceVariant = Color(0xFFF5EDEF)
    val OnSurfaceVariant = Color(0xFF4D4547)
    val SurfaceContainerLow = Color(0xFFF9F2F4)
    val SurfaceContainer = Color(0xFFF2EBED)
    val SurfaceContainerHigh = Color(0xFFEDE5E8)

    // Background
    val Background = Color(0xFFFFFBFA)
    val OnBackground = Color(0xFF1F1A1B)

    // Outline
    val Outline = Color(0xFF7D7476)
    val OutlineVariant = Color(0xFFCDC4C6)

    // Inverse
    val InverseSurface = Color(0xFF342F30)
    val InverseOnSurface = Color(0xFFF6EEF0)
    val InversePrimary = Color(0xFFFFB0CB)

    // =============================================
    // Dark Theme
    // =============================================

    val DarkPrimary = Color(0xFFFFB0CB)
    val DarkOnPrimary = Color(0xFF65002F)
    val DarkPrimaryContainer = Color(0xFF8E0048)
    val DarkOnPrimaryContainer = Color(0xFFFFD9E2)

    val DarkSecondary = Color(0xFFE5B8FF)
    val DarkOnSecondary = Color(0xFF4E006B)
    val DarkSecondaryContainer = Color(0xFF6E0095)
    val DarkOnSecondaryContainer = Color(0xFFF3D9FF)

    val DarkTertiary = Color(0xFFFFB59B)
    val DarkOnTertiary = Color(0xFF561700)
    val DarkTertiaryContainer = Color(0xFF7A2800)
    val DarkOnTertiaryContainer = Color(0xFFFFDBD0)

    val DarkError = Color(0xFFFFB4AB)
    val DarkOnError = Color(0xFF690005)
    val DarkErrorContainer = Color(0xFF93000A)
    val DarkOnErrorContainer = Color(0xFFFFDAD6)

    val DarkSuccess = Color(0xFF86EFAC)
    val DarkOnSuccess = Color(0xFF003D14)
    val DarkSuccessContainer = Color(0xFF166534)
    val DarkOnSuccessContainer = Color(0xFFDCFCE7)

    val DarkLike = Color(0xFF81C784)
    val DarkOnLike = Color(0xFF003300)
    val DarkPass = Color(0xFFEF9A9A)
    val DarkOnPass = Color(0xFF4A0000)
    val DarkSuperLike = Color(0xFF90CAF9)
    val DarkOnSuperLike = Color(0xFF001A3A)
    val DarkGold = Color(0xFFFFE082)
    val DarkOnGold = Color(0xFF3A2E00)

    val DarkSurface = Color(0xFF161314)
    val DarkOnSurface = Color(0xFFE9E1E3)
    val DarkSurfaceVariant = Color(0xFF30292B)
    val DarkOnSurfaceVariant = Color(0xFFCDC4C6)
    val DarkSurfaceContainerLow = Color(0xFF1F1A1B)
    val DarkSurfaceContainer = Color(0xFF231E20)
    val DarkSurfaceContainerHigh = Color(0xFF2E282A)

    val DarkBackground = Color(0xFF161314)
    val DarkOnBackground = Color(0xFFE9E1E3)

    val DarkOutline = Color(0xFF978E91)
    val DarkOutlineVariant = Color(0xFF4D4547)

    val DarkInverseSurface = Color(0xFFE9E1E3)
    val DarkInverseOnSurface = Color(0xFF342F30)
    val DarkInversePrimary = Color(0xFFE91E63)
}
