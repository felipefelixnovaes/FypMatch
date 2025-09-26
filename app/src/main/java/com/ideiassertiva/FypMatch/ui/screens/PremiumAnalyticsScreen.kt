package com.ideiassertiva.FypMatch.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme
import com.ideiassertiva.FypMatch.ui.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumAnalyticsScreen(
    onNavigateBack: () -> Unit = {},
    onUpgradeToPremium: () -> Unit = {},
    userId: String = "",
    currentSubscription: SubscriptionStatus = SubscriptionStatus.FREE,
    viewModel: AnalyticsViewModel = viewModel()
) {
    val analytics by viewModel.analytics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val isPremiumFeature = currentSubscription == SubscriptionStatus.FREE
    
    // Load analytics data
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            viewModel.loadAnalytics(userId)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Analytics Premium",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
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
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Premium Notice
                if (isPremiumFeature) {
                    item {
                        PremiumFeatureCard(
                            title = "Analytics Avançado",
                            description = "Veja estatísticas detalhadas do seu perfil e descubra insights para melhorar seus matches",
                            onUpgrade = onUpgradeToPremium
                        )
                    }
                } else {
                    // Profile Views Section
                    item {
                        ProfileViewsSection(analytics.profileViews)
                    }
                    
                    // Match Stats Section
                    item {
                        MatchStatsSection(analytics.matchStats)
                    }
                    
                    // Chat Stats Section
                    item {
                        ChatStatsSection(analytics.chatStats)
                    }
                    
                    // Activity Overview
                    item {
                        ActivityOverviewSection(analytics.activityStats)
                    }
                    
                    // Quick Stats Cards
                    item {
                        QuickStatsRow(analytics)
                    }
                    
                    // Insights & Tips
                    item {
                        InsightsTipsSection(analytics)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileViewsSection(viewStats: ProfileViewStats) {
    AnalyticsCard(
        title = "Visualizações do Perfil",
        icon = Icons.Default.Visibility
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "Total",
                value = viewStats.totalViews.toString(),
                color = MaterialTheme.colorScheme.primary
            )
            StatItem(
                label = "Hoje",
                value = viewStats.viewsToday.toString(),
                color = Color(0xFF4CAF50)
            )
            StatItem(
                label = "Esta Semana",
                value = viewStats.viewsThisWeek.toString(),
                color = Color(0xFF2196F3)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Horário de pico: ${viewStats.peakHour}h",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Dia favorito: ${viewStats.peakDay}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MatchStatsSection(matchStats: MatchStats) {
    AnalyticsCard(
        title = "Estatísticas de Matches",
        icon = Icons.Default.Favorite
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "Total Matches",
                value = matchStats.totalMatches.toString(),
                color = Color(0xFFE91E63)
            )
            StatItem(
                label = "Esta Semana",
                value = matchStats.matchesThisWeek.toString(),
                color = Color(0xFF9C27B0)
            )
            StatItem(
                label = "Taxa de Match",
                value = "${matchStats.matchRate.toInt()}%",
                color = Color(0xFF00BCD4)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LinearProgressIndicator(
            progress = (matchStats.matchRate / 100).toFloat(),
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFE91E63),
            trackColor = Color(0xFFE91E63).copy(alpha = 0.2f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Melhor horário: ${matchStats.bestMatchingTime}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChatStatsSection(chatStats: ChatStats) {
    AnalyticsCard(
        title = "Conversas",
        icon = Icons.Default.Chat
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "Conversas Iniciadas",
                value = chatStats.conversationsStarted.toString(),
                color = Color(0xFF4CAF50)
            )
            StatItem(
                label = "Taxa de Resposta",
                value = "${chatStats.responseRate.toInt()}%",
                color = Color(0xFF2196F3)
            )
            StatItem(
                label = "Tempo de Resposta",
                value = "${chatStats.averageResponseTime}min",
                color = Color(0xFFFF9800)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "${chatStats.activeConversations} conversas ativas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ActivityOverviewSection(activityStats: ActivityStats) {
    AnalyticsCard(
        title = "Atividade Geral",
        icon = Icons.Default.Analytics
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MiniStatCard(
                    label = "Dias Ativo",
                    value = activityStats.daysActive.toString(),
                    icon = Icons.Default.CalendarToday,
                    color = Color(0xFF4CAF50)
                )
            }
            item {
                MiniStatCard(
                    label = "Swipes Dados",
                    value = activityStats.totalSwipes.toString(),
                    icon = Icons.Default.SwipeRight,
                    color = Color(0xFF2196F3)
                )
            }
            item {
                MiniStatCard(
                    label = "Curtidas Dadas",
                    value = activityStats.likesGiven.toString(),
                    icon = Icons.Default.ThumbUp,
                    color = Color(0xFFE91E63)
                )
            }
            item {
                MiniStatCard(
                    label = "Curtidas Recebidas",
                    value = activityStats.likesReceived.toString(),
                    icon = Icons.Default.FavoriteBorder,
                    color = Color(0xFF9C27B0)
                )
            }
        }
    }
}

@Composable
private fun QuickStatsRow(analytics: ProfileAnalytics) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickStatCard(
            modifier = Modifier.weight(1f),
            title = "Popularidade",
            value = "${analytics.profileViews.totalViews}",
            subtitle = "visualizações",
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF4CAF50)
        )
        
        QuickStatCard(
            modifier = Modifier.weight(1f),
            title = "Engajamento",
            value = "${analytics.matchStats.matchRate.toInt()}%",
            subtitle = "taxa de match",
            icon = Icons.Default.Favorite,
            color = Color(0xFFE91E63)
        )
    }
}

@Composable
private fun InsightsTipsSection(analytics: ProfileAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFF2196F3)
                )
                Text(
                    text = "Insights & Dicas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val insights = generateInsights(analytics)
            insights.forEach { insight ->
                InsightItem(insight)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AnalyticsCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuickStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InsightItem(insight: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            modifier = Modifier.size(6.dp).padding(top = 6.dp),
            tint = Color(0xFF2196F3)
        )
        Text(
            text = insight,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun generateInsights(analytics: ProfileAnalytics): List<String> {
    val insights = mutableListOf<String>()
    
    if (analytics.profileViews.viewsToday > analytics.profileViews.averageViewsPerDay) {
        insights.add("Seu perfil está tendo mais visualizações que o normal hoje!")
    }
    
    if (analytics.matchStats.matchRate < 20.0) {
        insights.add("Considere adicionar mais fotos ou atualizar sua bio para melhorar sua taxa de match")
    }
    
    if (analytics.chatStats.responseRate < 50.0) {
        insights.add("Responder mais rapidamente pode melhorar suas chances de desenvolver conexões")
    }
    
    if (analytics.profileViews.peakHour in 18..22) {
        insights.add("Você recebe mais visualizações à noite - é o melhor momento para estar ativo!")
    }
    
    if (insights.isEmpty()) {
        insights.add("Seu perfil está performando bem! Continue ativo para manter o engajamento.")
    }
    
    return insights
}

@Composable
private fun PremiumFeatureCard(
    title: String,
    description: String,
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Button(
                onClick = onUpgrade,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                )
            ) {
                Text(
                    text = "Upgrade para Premium",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PremiumAnalyticsScreenPreview() {
    FypMatchTheme {
        PremiumAnalyticsScreen(
            currentSubscription = SubscriptionStatus.PREMIUM
        )
    }
}

@Preview(showBackground = true) 
@Composable
fun PremiumAnalyticsScreenFreePreview() {
    FypMatchTheme {
        PremiumAnalyticsScreen(
            currentSubscription = SubscriptionStatus.FREE
        )
    }
}