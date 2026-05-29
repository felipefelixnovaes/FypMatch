package com.ideiassertiva.FypMatch.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ideiassertiva.FypMatch.model.SubscriptionStatus
import com.ideiassertiva.FypMatch.model.PhotoUploadLimits

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
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = { Text("FypMatch Premium", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { PremiumHeader() }
            item { PremiumBenefits() }
            if (currentSubscription != SubscriptionStatus.FREE) {
                item { Phase5FeaturesSection(onNavigateToPhotoManager, onNavigateToFilters, onNavigateToAnalytics, currentSubscription) }
            }
            item { Text("Escolha seu plano", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
            item { PlanCard("Premium", "R$ 19,90", "/mês", listOf("100 curtidas/dia", "5 super curtidas/dia", "Ver quem te curtiu", "1 boost/mês (30 min)", "10 créditos IA/dia", "Sem anúncios", "Filtros avançados"), selectedPlan == SubscriptionStatus.PREMIUM, false) { selectedPlan = SubscriptionStatus.PREMIUM } }
            item { PlanCard("VIP", "R$ 39,90", "/mês", listOf("Curtidas ilimitadas", "Super curtidas ilimitadas", "5 boosts/mês + Super Boost 2h", "25 créditos IA/dia", "Prioridade no algoritmo", "Selo VIP exclusivo", "Analytics avançado", "Suporte prioritário"), selectedPlan == SubscriptionStatus.VIP, true) { selectedPlan = SubscriptionStatus.VIP } }
            item { Button(onClick = { onPurchase(selectedPlan) }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Assinar ${if (selectedPlan == SubscriptionStatus.PREMIUM) "Premium" else "VIP"}") } }
            item { Text("• Renovação automática\n• Cancele a qualquer momento", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
        }
    }
}

@Composable
private fun PremiumHeader() {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Box(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFFE91E63), Color(0xFF9C27B0)))).padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text("Desbloqueie todo o potencial do FypMatch", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Encontre o amor mais rápido com recursos premium", color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun PremiumBenefits() {
    Column { Text("Por que escolher Premium?", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp)); BenefitRow(BenefitItem(Icons.Default.Favorite, "Mais curtidas", "Até 100 por dia")); Spacer(Modifier.height(12.dp)); BenefitRow(BenefitItem(Icons.Default.Star, "Super curtidas", "Destaque-se")); Spacer(Modifier.height(12.dp)); BenefitRow(BenefitItem(Icons.Default.Person, "Ver quem curtiu", "Saiba quem se interessou")) }
}

@Composable private fun BenefitRow(b: BenefitItem) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Icon(b.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) { Text(b.title, fontWeight = FontWeight.Medium); Text(b.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun PlanCard(title: String, price: String, period: String, features: List<String>, isSelected: Boolean, isPopular: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)) else Modifier), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 4.dp), colors = CardDefaults.cardColors(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text(title, fontWeight = FontWeight.Bold); Row(verticalAlignment = Alignment.Bottom) { Text(price, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); Text(period, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                if (isPopular) Card(colors = CardDefaults.cardColors(Color(0xFFFF9800)), shape = RoundedCornerShape(12.dp)) { Text("POPULAR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
            }
            Spacer(Modifier.height(16.dp))
            features.forEach { f -> Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text(f) } }
        }
    }
}

private data class BenefitItem(val icon: ImageVector, val title: String, val description: String)

@Composable private fun Phase5FeaturesSection(onPM: () -> Unit, onF: () -> Unit, onA: () -> Unit, sub: SubscriptionStatus) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Star, null, tint = Color(0xFFFF9800), modifier = Modifier.size(28.dp)); Spacer(Modifier.width(8.dp)); Text("Recursos Premium", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureButton(Modifier.weight(1f), "Fotos", "Gerenciar", Icons.Filled.Image, Color(0xFF4CAF50), onPM)
                FeatureButton(Modifier.weight(1f), "Filtros", "Avançados", Icons.Filled.List, Color(0xFF2196F3), onF)
                FeatureButton(Modifier.weight(1f), "Analytics", "Estatísticas", Icons.Filled.Star, Color(0xFF9C27B0), onA)
            }
        }
    }
}

@Composable private fun FeatureButton(modifier: Modifier, title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(modifier.aspectRatio(1f).clickable { onClick() }, colors = CardDefaults.cardColors(color.copy(alpha = 0.1f)), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp)); Spacer(Modifier.height(8.dp)); Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = color, textAlign = TextAlign.Center); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}