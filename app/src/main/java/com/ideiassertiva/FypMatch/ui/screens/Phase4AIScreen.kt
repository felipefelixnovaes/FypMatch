package com.ideiassertiva.FypMatch.ui.screens
import androidx.compose.runtime.Composable

@Composable
fun Phase4AIScreen(
    onNavigateBack: () -> Unit = {},
    userId: String = ""
) {}

data class Phase4Stats(val totalAnalyses: Int = 0, val avgCompatibility: Float = 0f, val aiAccuracy: Float = 0f)
data class DemoResult(val type: String, val title: String, val description: String, val details: List<String> = emptyList())