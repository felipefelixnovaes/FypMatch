package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Phase5DemoScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPhotoManager: () -> Unit = {},
    onNavigateToFilters: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    currentSubscription: SubscriptionStatus = SubscriptionStatus.FREE
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Fase 5 - Premium Features",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Recursos premium avançados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Section
            item {
                Phase5HeroCard(currentSubscription)
            }
            
            // Implementation Status
            item {
                ImplementationStatusCard()
            }
            
            // Feature Categories
            val featureCategories = listOf(
                FeatureCategory(
                    title = "Gerenciamento de Fotos",
                    description = "Sistema completo para upload e organização de múltiplas fotos",
                    icon = Icons.Default.Image,
                    color = Color(0xFF4CAF50),
                    features = listOf(
                        "Upload de até 6/12/20 fotos (Free/Premium/VIP)",
                        "Detecção automática de qualidade",
                        "Definir foto principal",
                        "Suporte a fotos HD (Premium+)",
                        "Gerenciamento visual em grade"
                    ),
                    onNavigate = onNavigateToPhotoManager,
                    isImplemented = true
                ),
                FeatureCategory(
                    title = "Filtros Avançados",
                    description = "Sistema de filtragem detalhado para encontrar matches perfeitos",
                    icon = Icons.Default.FilterList,
                    color = Color(0xFF2196F3),
                    features = listOf(
                        "Filtros por idade e distância",
                        "Estilo de vida (fumo, bebida)",
                        "Família e valores (filhos, religião)",
                        "Verificação e atividade",
                        "Altura e características físicas"
                    ),
                    onNavigate = onNavigateToFilters,
                    isImplemented = true
                ),
                FeatureCategory(
                    title = "Analytics Premium",
                    description = "Dashboard com insights detalhados do seu perfil",
                    icon = Icons.Default.Analytics,
                    color = Color(0xFF9C27B0),
                    features = listOf(
                        "Visualizações do perfil detalhadas",
                        "Estatísticas de matches e taxa de sucesso",
                        "Análise de conversas e tempo de resposta",
                        "Insights e dicas personalizadas",
                        "Horários de pico e tendências"
                    ),
                    onNavigate = onNavigateToAnalytics,
                    isImplemented = true
                ),
                FeatureCategory(
                    title = "Boost e Visibilidade",
                    description = "Sistema de boost para aumentar a visibilidade do perfil",
                    icon = Icons.Default.TrendingUp,
                    color = Color(0xFFFF9800),
                    features = listOf(
                        "Boost regular (30 min)",
                        "Super boost (2h - VIP only)",
                        "Prioridade no algoritmo",
                        "Analytics de performance do boost",
                        "Agendamento de boosts"
                    ),
                    onNavigate = { /* Future implementation */ },
                    isImplemented = false
                ),
                FeatureCategory(
                    title = "Sistema de Verificação",
                    description = "Badges e verificações para aumentar a credibilidade",
                    icon = Icons.Default.Verified,
                    color = Color(0xFF00BCD4),
                    features = listOf(
                        "Verificação por foto/selfie",
                        "Verificação por documento",
                        "Badges LGBTQIA+ e neurodiversidade",
                        "Selo de usuário ativo",
                        "Badges premium visíveis"
                    ),
                    onNavigate = { /* Future implementation */ },
                    isImplemented = false
                )
            )
            
            items(featureCategories) { category ->
                FeatureCategoryCard(
                    category = category,
                    isAccessible = currentSubscription != SubscriptionStatus.FREE || category.title == "Boost e Visibilidade",
                    onUpgradeToPremium = onNavigateToPremium
                )
            }
            
            // Next Steps
            item {
                NextStepsCard()
            }
        }
    }
}

@Composable
private fun Phase5HeroCard(subscription: SubscriptionStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFE91E63),
                            Color(0xFF9C27B0),
                            Color(0xFF2196F3)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "🎉 Fase 5 Implementada!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Recursos premium avançados agora disponíveis",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Status: ${subscription.name}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImplementationStatusCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Status da Implementação",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val implementations = listOf(
                "✅ Modelos de dados para recursos premium" to true,
                "✅ Sistema de gerenciamento de fotos múltiplas" to true,
                "✅ Filtros avançados com interface completa" to true,
                "✅ Dashboard de analytics premium" to true,
                "✅ ViewModels e lógica de negócio" to true,
                "🔄 Sistema de boost (planejado)" to false,
                "🔄 Verificação de badges (planejado)" to false
            )
            
            implementations.forEach { (item, isCompleted) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurface 
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureCategoryCard(
    category: FeatureCategory,
    isAccessible: Boolean,
    onUpgradeToPremium: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isAccessible && category.isImplemented) { 
                category.onNavigate() 
            },
        colors = CardDefaults.cardColors(
            containerColor = if (category.isImplemented) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (category.isImplemented) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = category.color,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Column {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = category.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Status badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (category.isImplemented) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFFF9800)
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (category.isImplemented) "PRONTO" else "EM BREVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Features list
            category.features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = if (category.isImplemented) Icons.Default.Check else Icons.Default.Circle,
                        contentDescription = null,
                        tint = if (category.isImplemented) Color(0xFF4CAF50) else Color.Gray,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (category.isImplemented) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            // Premium upgrade button for free users
            if (!isAccessible && category.isImplemented) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onUpgradeToPremium,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = category.color
                    )
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upgrade para acessar")
                }
            }
        }
    }
}

@Composable
private fun NextStepsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🚀 Próximos Passos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val nextSteps = listOf(
                "Implementar sistema de boost em tempo real",
                "Adicionar verificação de badges automatizada",
                "Desenvolver notificações push para analytics",
                "Integrar sistema de pagamento para boost",
                "Adicionar testes unitários para todos os recursos"
            )
            
            nextSteps.forEach { step ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("• ", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private data class FeatureCategory(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val features: List<String>,
    val onNavigate: () -> Unit,
    val isImplemented: Boolean
)

@Preview(showBackground = true)
@Composable
fun Phase5DemoScreenPreview() {
    FypMatchTheme {
        Phase5DemoScreen(
            currentSubscription = SubscriptionStatus.PREMIUM
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Phase5DemoScreenFreePreview() {
    FypMatchTheme {
        Phase5DemoScreen(
            currentSubscription = SubscriptionStatus.FREE
        )
    }
}