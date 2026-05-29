package com.ideiassertiva.FypMatch.ui.screens

import android.content.Context
import android.content.SharedPreferences
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ideiassertiva.FypMatch.ui.components.FypGradientButton
import com.ideiassertiva.FypMatch.ui.theme.MatchPink40
import com.ideiassertiva.FypMatch.ui.theme.MatchPurple40
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private const val MAX_ADS_PER_DAY = 3
private const val CREDITS_PER_AD = 3

private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
private fun adsWatchedKey(userId: String) = "ads_watched_${userId}_${todayKey()}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdsScreen(
    onNavigateBack: () -> Unit,
    userId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs: SharedPreferences = remember {
        context.getSharedPreferences("fypmatch_ads", Context.MODE_PRIVATE)
    }

    var adsWatched by remember { mutableIntStateOf(prefs.getInt(adsWatchedKey(userId), 0)) }
    var isWatching by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val canWatch = adsWatched < MAX_ADS_PER_DAY

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
            Text("🎬", fontSize = 64.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(
                "Assista e Ganhe Créditos",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Assista a um anúncio curto e ganhe $CREDITS_PER_AD créditos\npara usar com seu conselheiro IA",
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
                        Text("$adsWatched/$MAX_ADS_PER_DAY",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MatchPink40)
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { adsWatched.toFloat() / MAX_ADS_PER_DAY.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MatchPink40,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧠 ", fontSize = 14.sp)
                        Text(
                            "${adsWatched * CREDITS_PER_AD} créditos ganhos hoje",
                            style = MaterialTheme.typography.bodySmall,
                            color = MatchPurple40
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Estado: assistindo / sucesso / botão
            when {
                isWatching -> WatchingAdIndicator()
                showSuccess -> SuccessCreditsCard(credits = CREDITS_PER_AD) { showSuccess = false }
                else -> {
                    FypGradientButton(
                        text = if (canWatch) "Assistir Anúncio (+$CREDITS_PER_AD créditos)"
                               else "Limite diário atingido ✓",
                        enabled = canWatch,
                        onClick = {
                            if (canWatch) {
                                isWatching = true
                                errorMessage = null
                                // Simula anúncio (5 segundos)
                                val job = kotlinx.coroutines.MainScope().launch {
                                    delay(5000L)
                                    val key = adsWatchedKey(userId)
                                    val newCount = adsWatched + 1
                                    prefs.edit().putInt(key, newCount).apply()
                                    adsWatched = newCount
                                    isWatching = false
                                    showSuccess = true
                                }
                            }
                        }
                    )

                    if (!canWatch) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🕐 ", fontSize = 14.sp)
                            Text("Volte amanhã para mais créditos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            errorMessage?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
                ) {
                    Text(msg, modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF856404))
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
                    color = MatchPink40,
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Check, "Sucesso",
                modifier = Modifier.size(48.dp), tint = Color(0xFF4CAF50))
            Spacer(Modifier.height(12.dp))
            Text("+$credits créditos adicionados!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2E7D32))
            Text("Use com seu conselheiro IA",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF388E3C))
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onDismiss) {
                Text("Continuar", color = MatchPink40, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PlanCreditsItem(plan: String, credits: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(plan, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        Text(credits, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
