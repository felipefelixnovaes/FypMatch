package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Phase4AIScreen(
    onNavigateBack: () -> Unit = {},
    userId: String = ""
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Análise IA Avançada") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text("🧠", fontSize = 64.sp, textAlign = TextAlign.Center)
            Text(
                "Análise de Personalidade IA",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Complete o questionário profundo para desbloquear sua análise completa de compatibilidade por IA.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            listOf(
                "🎯" to "Perfil neuro-comportamental",
                "💞" to "Compatibilidade preditiva",
                "🔮" to "Padrões de relacionamento",
                "📊" to "Score multidimensional"
            ).forEach { (emoji, label) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(emoji, fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

data class Phase4Stats(val totalAnalyses: Int = 0, val avgCompatibility: Float = 0f, val aiAccuracy: Float = 0f)
data class DemoResult(val type: String, val title: String, val description: String, val details: List<String> = emptyList())
