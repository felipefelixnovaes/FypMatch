package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme
import com.ideiassertiva.FypMatch.ui.viewmodel.Phase4ViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Phase4AIScreen(
    onNavigateBack: () -> Unit = {},
    userId: String = "",
    viewModel: Phase4ViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val personalityProfile by viewModel.personalityProfile.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    // Initialize user data when screen loads
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            viewModel.initializeUser(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("IA Avançada - Fase 4")
                        Text(
                            "Análise Neural & Compatibilidade ML",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Section
            item {
                Phase4HeroCard(isAnalyzing = isAnalyzing)
            }

            // Personality Analysis Section
            item {
                PersonalityAnalysisCard(
                    personalityProfile = personalityProfile,
                    isAnalyzing = isAnalyzing,
                    onAnalyze = { viewModel.analyzePersonality() }
                )
            }

            // AI Features Section
            item {
                AIFeaturesGrid(
                    onCompatibilityAnalysis = { viewModel.runCompatibilityDemo() },
                    onNeuroSupport = { viewModel.showNeuroSupportDemo() },
                    onSmartSuggestions = { viewModel.generateSmartSuggestions() },
                    onBehaviorAnalysis = { viewModel.analyzeBehaviorPatterns() }
                )
            }

            // Results Section
            if (uiState.demoResults.isNotEmpty()) {
                items(uiState.demoResults) { result ->
                    DemoResultCard(result = result)
                }
            }

            // Phase 4 Stats
            item {
                Phase4StatsCard(stats = uiState.phase4Stats)
            }
        }
    }
}

@Composable
private fun Phase4HeroCard(isAnalyzing: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "🤖 Assistente Neural Avançado",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "IA que aprende com seu comportamento para otimizar matches e conversas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                if (isAnalyzing) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Analisando dados...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalityAnalysisCard(
    personalityProfile: PersonalityProfile?,
    isAnalyzing: Boolean,
    onAnalyze: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 Análise de Personalidade",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (!isAnalyzing) {
                    TextButton(onClick = onAnalyze) {
                        Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Analisar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (personalityProfile != null) {
                PersonalityTraitsDisplay(personalityProfile.traits)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                personalityProfile.mbtiType?.let { mbti ->
                    PersonalityBadge(mbti, personalityProfile.confidence)
                }
            } else {
                Text(
                    text = "Clique em 'Analisar' para descobrir insights sobre sua personalidade baseados em IA",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PersonalityTraitsDisplay(traits: PersonalityTraits) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TraitBar("Extroversão", traits.extraversion, Icons.Default.Group)
        TraitBar("Amabilidade", traits.agreeableness, Icons.Default.Favorite)
        TraitBar("Conscienciosidade", traits.conscientiousness, Icons.Default.CheckCircle)
        TraitBar("Estabilidade", 1f - traits.neuroticism, Icons.Default.Mood)
        TraitBar("Abertura", traits.openness, Icons.Default.Explore)
    }
}

@Composable
private fun TraitBar(
    name: String,
    value: Float,
    icon: ImageVector
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = value,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun PersonalityBadge(mbti: String, confidence: Float) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tipo: $mbti",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Confiança: ${(confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun AIFeaturesGrid(
    onCompatibilityAnalysis: () -> Unit,
    onNeuroSupport: () -> Unit,
    onSmartSuggestions: () -> Unit,
    onBehaviorAnalysis: () -> Unit
) {
    Text(
        text = "🚀 Recursos de IA Avançada",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AIFeatureCard(
                title = "Compatibilidade ML",
                description = "Análise avançada de compatibilidade",
                icon = Icons.Default.Psychology,
                onClick = onCompatibilityAnalysis,
                modifier = Modifier.weight(1f)
            )
            
            AIFeatureCard(
                title = "Suporte Neuro",
                description = "Assistência para neurodiversidade",
                icon = Icons.Default.Accessibility,
                onClick = onNeuroSupport,
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AIFeatureCard(
                title = "Sugestões Smart",
                description = "IA para conversas",
                icon = Icons.Default.Lightbulb,
                onClick = onSmartSuggestions,
                modifier = Modifier.weight(1f)
            )
            
            AIFeatureCard(
                title = "Análise Comportamental",
                description = "Padrões de swipe",
                icon = Icons.Default.Analytics,
                onClick = onBehaviorAnalysis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AIFeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DemoResultCard(result: DemoResult) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (result.type) {
                    "compatibility" -> MaterialTheme.colorScheme.primaryContainer
                    "neurosupport" -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (result.type) {
                            "compatibility" -> Icons.Default.Psychology
                            "neurosupport" -> Icons.Default.Accessibility
                            "suggestions" -> Icons.Default.Lightbulb
                            else -> Icons.Default.Analytics
                        },
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = result.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (result.details.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(result.details) { detail ->
                            DetailChip(detail)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailChip(detail: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = detail,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun Phase4StatsCard(stats: Phase4Stats) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📈 Estatísticas da Fase 4",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Análises", stats.totalAnalyses.toString())
                StatItem("Compatibilidade Média", "${(stats.avgCompatibility * 100).toInt()}%")
                StatItem("Precisão IA", "${(stats.aiAccuracy * 100).toInt()}%")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Data classes for the UI
data class DemoResult(
    val type: String,
    val title: String,
    val description: String,
    val details: List<String> = emptyList()
)

data class Phase4Stats(
    val totalAnalyses: Int = 0,
    val avgCompatibility: Float = 0.0f,
    val aiAccuracy: Float = 0.0f
)

@Preview
@Composable
private fun Phase4AIScreenPreview() {
    FypMatchTheme {
        Phase4AIScreen(userId = "preview_user")
    }
}