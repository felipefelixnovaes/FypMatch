package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ideiassertiva.FypMatch.ui.components.FypGradientButton
import com.ideiassertiva.FypMatch.ui.theme.FypColors
import com.ideiassertiva.FypMatch.ui.viewmodel.AdsViewModel
import com.ideiassertiva.FypMatch.model.AiCreditLimits
import com.ideiassertiva.FypMatch.ui.util.findActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdsScreen(
    onNavigateBack: () -> Unit,
    userId: String,
    modifier: Modifier = Modifier,
    viewModel: AdsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val maxAdsPerDay = uiState.maxAdsPerDay.coerceAtLeast(1)
    val canWatch = uiState.canWatchMore
    val activity = LocalContext.current.findActivity()

    LaunchedEffect(userId) {
        viewModel.load(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ganhar Créditos IA") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Header
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text(
                "Assista e Ganhe Créditos",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Assista a um anuncio curto e ganhe ${AiCreditLimits.AD_REWARD} creditos\npara usar com seu conselheiro IA",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // Progresso
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Anúncios hoje",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("${uiState.adsWatchedToday}/$maxAdsPerDay",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = FypColors.Primary)
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { uiState.adsWatchedToday.toFloat() / maxAdsPerDay.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = FypColors.Primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧠 ", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                        Text(
                            "${uiState.creditsEarnedToday} creditos ganhos hoje • ${uiState.credits.current} disponiveis",
                            style = MaterialTheme.typography.bodySmall,
                            color = FypColors.Secondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Estado: assistindo / sucesso / botão
            when {
                uiState.isWatchingAd -> WatchingAdIndicator()
                uiState.showSuccess -> SuccessCreditsCard(credits = uiState.lastEarnedCredits) {
                    viewModel.dismissSuccess()
                }
                else -> {
                    FypGradientButton(
                        text = if (canWatch) "Assistir Anuncio (+${AiCreditLimits.AD_REWARD} creditos)"
                               else "Limite diario atingido",
                        enabled = canWatch,
                        onClick = {
                            if (canWatch) {
                                viewModel.watchAd(userId, activity)
                            }
                        }
                    )

                    if (!canWatch) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🕐 ", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                            Text("Volte amanhã para mais créditos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            uiState.error?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = FypColors.Gold.copy(alpha = 0.15f))
                ) {
                    Text(msg, modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = FypColors.Gold)
                }
            }

            Spacer(Modifier.height(28.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Nota sobre planos
            Text("Créditos por plano",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PlanCreditsItem("Gratuito", "Via anúncios", "▶️")
                PlanCreditsItem("Premium", "10/dia", "👑")
                PlanCreditsItem("VIP", "25/dia", "💎")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun WatchingAdIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "progress")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(72.dp),
                    color = FypColors.Primary,
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round
                )
                Text("${((1f - progress) * 5).toInt() + 1}s",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(Modifier.height(16.dp))
            Text("Carregando anúncio...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text("Não feche esta tela",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun SuccessCreditsCard(credits: Int, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FypColors.SuccessContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Check, "Sucesso",
                modifier = Modifier.size(48.dp), tint = FypColors.Like)
            Spacer(Modifier.height(12.dp))
            Text("+$credits créditos adicionados!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = FypColors.Success)
            Text("Use com seu conselheiro IA",
                style = MaterialTheme.typography.bodySmall,
                color = FypColors.Success)
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onDismiss) {
                Text("Continuar", color = FypColors.Primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PlanCreditsItem(plan: String, credits: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = MaterialTheme.typography.titleLarge.fontSize)
        Spacer(Modifier.height(4.dp))
        Text(plan, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        Text(credits, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
