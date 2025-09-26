package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import com.ideiassertiva.FypMatch.model.SubscriptionStatus
import com.ideiassertiva.FypMatch.model.PhotoUploadLimits
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit = {},
    onPurchase: (SubscriptionStatus) -> Unit = {},
    onNavigateToPhotoManager: () -> Unit = {},
    onNavigateToFilters: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    currentSubscription: SubscriptionStatus = SubscriptionStatus.FREE
) {
    var selectedPlan by remember { mutableStateOf(SubscriptionStatus.PREMIUM) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "FypMatch Premium",
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
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header Premium
                PremiumHeader()
            }
            
            item {
                // Benefícios
                PremiumBenefits()
            }
            
            // Phase 5 Premium Features (only show if user has premium)
            if (currentSubscription != SubscriptionStatus.FREE) {
                item {
                    Phase5FeaturesSection(
                        onNavigateToPhotoManager = onNavigateToPhotoManager,
                        onNavigateToFilters = onNavigateToFilters,
                        onNavigateToAnalytics = onNavigateToAnalytics,
                        subscription = currentSubscription
                    )
                }
            }
            
            item {
                // Planos
                Text(
                    text = "Escolha seu plano",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                // Plano Premium
                PlanCard(
                    title = "Premium",
                    price = "R$ 19,90",
                    period = "/mês",
                    features = listOf(
                        "100 curtidas por dia",
                        "5 super curtidas por dia",
                        "Ver quem te curtiu",
                        "🧠 10 créditos IA por dia",
                        "Boost mensal grátis",
                        "Sem anúncios"
                    ),
                    isSelected = selectedPlan == SubscriptionStatus.PREMIUM,
                    isPopular = true,
                    onClick = { selectedPlan = SubscriptionStatus.PREMIUM }
                )
            }
            
            item {
                // Plano VIP
                PlanCard(
                    title = "VIP",
                    price = "R$ 39,90",
                    period = "/mês",
                    features = listOf(
                        "Curtidas ilimitadas",
                        "Super curtidas ilimitadas",
                        "Ver quem te curtiu",
                        "5 boosts por mês",
                        "Prioridade no algoritmo",
                        "🧠 25 créditos IA por dia",
                        "Acesso antecipado a novidades",
                        "Suporte prioritário",
                        "Selo VIP no perfil"
                    ),
                    isSelected = selectedPlan == SubscriptionStatus.VIP,
                    isPopular = false,
                    onClick = { selectedPlan = SubscriptionStatus.VIP }
                )
            }
            
            item {
                // Botão de compra
                Button(
                    onClick = { onPurchase(selectedPlan) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Assinar ${if (selectedPlan == SubscriptionStatus.PREMIUM) "Premium" else "VIP"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            item {
                // Termos
                Text(
                    text = "• Renovação automática\n• Cancele a qualquer momento\n• Política de privacidade e termos de uso aplicáveis",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PremiumHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                            Color(0xFFE91E63),
                            Color(0xFF9C27B0)
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Desbloqueie todo o potencial do FypMatch",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Encontre o amor mais rápido com recursos premium",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PremiumBenefits() {
    Column {
        Text(
            text = "Por que escolher Premium?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        val benefits = listOf(
            BenefitItem(Icons.Default.Favorite, "Mais curtidas", "Até 100 curtidas por dia vs 10 grátis"),
            BenefitItem(Icons.Default.Star, "Super curtidas", "Destaque-se com super curtidas"),
            BenefitItem(Icons.Default.Person, "Ver quem curtiu", "Veja quem já demonstrou interesse"),
            BenefitItem(Icons.Default.Settings, "Boost do perfil", "Apareça para mais pessoas"),
            BenefitItem(Icons.Default.Email, "Sem anúncios", "Experiência limpa e focada")
        )
        
        benefits.forEach { benefit ->
            BenefitRow(benefit)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun BenefitRow(benefit: BenefitItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = benefit.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = benefit.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = benefit.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanCard(
    title: String,
    price: String,
    period: String,
    features: List<String>,
    isSelected: Boolean,
    isPopular: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header do plano
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = price,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = period,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (isPopular) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFF9800)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "POPULAR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Features
            features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private data class BenefitItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@Composable
private fun Phase5FeaturesSection(
    onNavigateToPhotoManager: () -> Unit,
    onNavigateToFilters: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    subscription: SubscriptionStatus
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Recursos Premium Avançados",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Aproveite todos os recursos exclusivos da sua assinatura ${subscription.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Feature Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureButton(
                    modifier = Modifier.weight(1f),
                    title = "Fotos",
                    subtitle = "Gerenciar",
                    icon = Icons.Default.Image,
                    color = Color(0xFF4CAF50),
                    onClick = onNavigateToPhotoManager
                )
                
                FeatureButton(
                    modifier = Modifier.weight(1f),
                    title = "Filtros",
                    subtitle = "Avançados",
                    icon = Icons.Default.FilterList,
                    color = Color(0xFF2196F3),
                    onClick = onNavigateToFilters
                )
                
                FeatureButton(
                    modifier = Modifier.weight(1f),
                    title = "Analytics",
                    subtitle = "Estatísticas",
                    icon = Icons.Default.Analytics,
                    color = Color(0xFF9C27B0),
                    onClick = onNavigateToAnalytics
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Premium features list
            val premiumFeatures = listOf(
                "Até ${PhotoUploadLimits.forSubscription(subscription).maxPhotos} fotos em alta qualidade",
                "Filtros avançados por estilo de vida",
                "Analytics detalhado do seu perfil",
                "Boosts ${if (subscription == SubscriptionStatus.VIP) "ilimitados" else "mensais"}",
                "Prioridade no algoritmo de descoberta"
            )
            
            premiumFeatures.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                fontWeight = FontWeight.SemiBold,
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PremiumScreenPreview() {
    FypMatchTheme {
        PremiumScreen()
    }
} 
