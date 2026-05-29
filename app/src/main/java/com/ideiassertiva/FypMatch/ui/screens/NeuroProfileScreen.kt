package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ideiassertiva.FypMatch.model.Accommodation
import com.ideiassertiva.FypMatch.model.AccommodationType
import com.ideiassertiva.FypMatch.model.NeuroPreferences
import com.ideiassertiva.FypMatch.model.NeuroProfile
import com.ideiassertiva.FypMatch.ui.components.FypGradientButton
import com.ideiassertiva.FypMatch.ui.theme.MatchPink40
import com.ideiassertiva.FypMatch.ui.theme.MatchPurple40

// ─── Dados internos da tela ──────────────────────────────────────────────────

private data class NeurotipoItem(val label: String, val emoji: String)

private val neurotipos = listOf(
    NeurotipoItem("TDAH", "⚡"),
    NeurotipoItem("Autismo (TEA)", "🧩"),
    NeurotipoItem("Dislexia", "📖"),
    NeurotipoItem("Ansiedade", "💭"),
    NeurotipoItem("TOC", "🔄"),
    NeurotipoItem("Bipolaridade", "🌊"),
    NeurotipoItem("Depressão", "🌧"),
    NeurotipoItem("Processamento Sensorial", "🎵"),
    NeurotipoItem("Outro", "✨")
)

private data class PreferenciaSwitch(
    val titulo: String,
    val subtitulo: String,
    val campo: String
)

private val preferencias = listOf(
    PreferenciaSwitch("Comunicação direta", "Prefiro mensagens claras e objetivas", "prefersDirectness"),
    PreferenciaSwitch("Mais tempo para processar", "Preciso de tempo antes de responder", "needsRoutine"),
    PreferenciaSwitch("Evitar críticas diretas", "Sou sensível a feedback negativo", "sensitiveToCriticism"),
    PreferenciaSwitch("Texto ao invés de chamadas", "Prefiro mensagens escritas", "prefersTextOverVoice"),
    PreferenciaSwitch("Rotina e previsibilidade", "Gosto de saber o que esperar", "needsClearCommunication")
)

private data class AcomodacaoCard(
    val label: String,
    val emoji: String,
    val tipo: AccommodationType
)

private val acomodacoes = listOf(
    AcomodacaoCard("Interface simplificada", "📱", AccommodationType.SIMPLIFIED_INTERFACE),
    AcomodacaoCard("Menos estímulos", "🔕", AccommodationType.REDUCED_STIMULATION),
    AcomodacaoCard("Instruções claras", "📋", AccommodationType.CLEAR_INSTRUCTIONS),
    AcomodacaoCard("Filtro sensorial", "🎛", AccommodationType.SENSORY_FILTERING)
)

// ─── Tela principal ───────────────────────────────────────────────────────────

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NeuroProfileScreen(
    onNavigateBack: () -> Unit,
    onComplete: (NeuroProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    // ── Estado local (sobrevive a rotação) ────────────────────────────────────
    var currentStep by rememberSaveable { mutableStateOf(0) }

    // Step 1 — neurotipos selecionados
    val neurotiposSelecionados = rememberSaveable { mutableStateListOf<String>() }

    // Step 2 — preferências de comunicação
    var prefDirectness by rememberSaveable { mutableStateOf(false) }
    var prefRoutine by rememberSaveable { mutableStateOf(false) }
    var prefCriticism by rememberSaveable { mutableStateOf(false) }
    var prefText by rememberSaveable { mutableStateOf(false) }
    var prefClearComm by rememberSaveable { mutableStateOf(false) }

    // Step 3 — acomodações
    val acomodacoesSelecionadas = rememberSaveable { mutableStateListOf<AccommodationType>() }

    fun buildProfile(): NeuroProfile = NeuroProfile(
        preferences = NeuroPreferences(
            needsClearCommunication = prefClearComm,
            prefersDirectness = prefDirectness,
            sensitiveToCriticism = prefCriticism,
            needsRoutine = prefRoutine,
            prefersTextOverVoice = prefText
        ),
        accommodations = acomodacoesSelecionadas.map { tipo ->
            Accommodation(type = tipo, description = "")
        }
    )

    Scaffold(
        topBar = {
            if (currentStep > 0) {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("Perfil de Neurodiversidade") },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (currentStep > 0) currentStep-- else onNavigateBack()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                )
            }
        },
        modifier = modifier
    ) { padding ->
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                val entra = slideInHorizontally { it } + fadeIn()
                val sai = slideOutHorizontally { -it } + fadeOut()
                entra togetherWith sai
            },
            label = "neuro_step_anim"
        ) { step ->
            when (step) {
                0 -> StepBoasVindas(
                    paddingValues = padding,
                    onComecar = { currentStep = 1 },
                    onPular = { onComplete(NeuroProfile()) }
                )
                1 -> StepNeurotipos(
                    paddingValues = padding,
                    selecionados = neurotiposSelecionados,
                    onToggle = { label ->
                        if (neurotiposSelecionados.contains(label))
                            neurotiposSelecionados.remove(label)
                        else
                            neurotiposSelecionados.add(label)
                    },
                    onNext = { currentStep = 2 }
                )
                2 -> StepPreferencias(
                    paddingValues = padding,
                    prefDirectness = prefDirectness, onDirectness = { prefDirectness = it },
                    prefRoutine = prefRoutine, onRoutine = { prefRoutine = it },
                    prefCriticism = prefCriticism, onCriticism = { prefCriticism = it },
                    prefText = prefText, onText = { prefText = it },
                    prefClearComm = prefClearComm, onClearComm = { prefClearComm = it },
                    onNext = { currentStep = 3 }
                )
                3 -> StepAcomodacoes(
                    paddingValues = padding,
                    selecionadas = acomodacoesSelecionadas,
                    onToggle = { tipo ->
                        if (acomodacoesSelecionadas.contains(tipo))
                            acomodacoesSelecionadas.remove(tipo)
                        else
                            acomodacoesSelecionadas.add(tipo)
                    },
                    onNext = { currentStep = 4 }
                )
                4 -> StepResumo(
                    paddingValues = padding,
                    totalPreferencias = listOf(prefDirectness, prefRoutine, prefCriticism, prefText, prefClearComm).count { it },
                    totalAcomodacoes = acomodacoesSelecionadas.size,
                    onSalvar = { onComplete(buildProfile()) }
                )
            }
        }
    }
}

// ─── Step 0 — Boas-vindas ─────────────────────────────────────────────────────

@Composable
private fun StepBoasVindas(
    paddingValues: PaddingValues,
    onComecar: () -> Unit,
    onPular: () -> Unit
) {
    val gradient = Brush.verticalGradient(listOf(MatchPink40, MatchPurple40))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero com gradiente — 40% da tela
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(0.4f)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🧠", fontSize = 72.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Perfil de Neurodiversidade",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Este espaço é seu. Compartilhe o quanto quiser — todas as informações são privadas.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(Modifier.height(40.dp))

        FypGradientButton(
            text = "Começar",
            onClick = onComecar,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onPular) {
            Text(
                text = "Pular por agora",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ─── Step 1 — Tipo de neurodiversidade ───────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepNeurotipos(
    paddingValues: PaddingValues,
    selecionados: List<String>,
    onToggle: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { 0.25f },
            modifier = Modifier.fillMaxWidth(),
            color = MatchPink40,
            trackColor = MatchPink40.copy(alpha = 0.2f)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Como você se identifica?",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Selecione uma ou mais opções. Você pode alterar isso depois.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            neurotipos.forEach { item ->
                val selecionado = selecionados.contains(item.label)
                FilterChip(
                    selected = selecionado,
                    onClick = { onToggle(item.label) },
                    label = { Text("${item.emoji} ${item.label}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MatchPink40.copy(alpha = 0.15f),
                        selectedLabelColor = MatchPink40
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selecionado,
                        selectedBorderColor = MatchPink40,
                        borderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        FypGradientButton(
            text = "Próximo →",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
    }
}

// ─── Step 2 — Preferências de comunicação ────────────────────────────────────

@Composable
private fun StepPreferencias(
    paddingValues: PaddingValues,
    prefDirectness: Boolean, onDirectness: (Boolean) -> Unit,
    prefRoutine: Boolean, onRoutine: (Boolean) -> Unit,
    prefCriticism: Boolean, onCriticism: (Boolean) -> Unit,
    prefText: Boolean, onText: (Boolean) -> Unit,
    prefClearComm: Boolean, onClearComm: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    data class SwitchItem(
        val titulo: String,
        val subtitulo: String,
        val valor: Boolean,
        val onChange: (Boolean) -> Unit
    )

    val items = listOf(
        SwitchItem("Comunicação direta", "Prefiro mensagens claras e objetivas", prefDirectness, onDirectness),
        SwitchItem("Mais tempo para processar", "Preciso de tempo antes de responder", prefRoutine, onRoutine),
        SwitchItem("Evitar críticas diretas", "Sou sensível a feedback negativo", prefCriticism, onCriticism),
        SwitchItem("Texto ao invés de chamadas", "Prefiro mensagens escritas", prefText, onText),
        SwitchItem("Rotina e previsibilidade", "Gosto de saber o que esperar", prefClearComm, onClearComm)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        LinearProgressIndicator(
            progress = { 0.50f },
            modifier = Modifier.fillMaxWidth(),
            color = MatchPink40,
            trackColor = MatchPink40.copy(alpha = 0.2f)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Como você prefere se comunicar?",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(24.dp))

        items.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.titulo,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = item.subtitulo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = item.valor,
                        onCheckedChange = item.onChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MatchPink40
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        FypGradientButton(
            text = "Próximo →",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
    }
}

// ─── Step 3 — Acomodações de interface ───────────────────────────────────────

@Composable
private fun StepAcomodacoes(
    paddingValues: PaddingValues,
    selecionadas: List<AccommodationType>,
    onToggle: (AccommodationType) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        LinearProgressIndicator(
            progress = { 0.75f },
            modifier = Modifier.fillMaxWidth(),
            color = MatchPink40,
            trackColor = MatchPink40.copy(alpha = 0.2f)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Como posso te ajudar?",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Selecione as acomodações que fazem sentido para você.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        // Grid 2x2
        val pares = acomodacoes.chunked(2)
        pares.forEach { par ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                par.forEach { item ->
                    val selecionado = selecionadas.contains(item.tipo)
                    val borderColor = if (selecionado) MatchPink40 else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    val bgColor = if (selecionado) MatchPink40.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .border(
                                width = if (selecionado) 2.dp else 1.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onToggle(item.tipo) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(item.emoji, fontSize = 32.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selecionado) MatchPink40 else MaterialTheme.colorScheme.onSurface
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                // Preencher espaço se par tiver só 1 item
                if (par.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(32.dp))

        FypGradientButton(
            text = "Próximo →",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
    }
}

// ─── Step 4 — Resumo e confirmação ───────────────────────────────────────────

@Composable
private fun StepResumo(
    paddingValues: PaddingValues,
    totalPreferencias: Int,
    totalAcomodacoes: Int,
    onSalvar: () -> Unit
) {
    // Animação de entrada do ícone ✅
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "check_scale"
    )
    var triggerAnim by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (triggerAnim) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "check_entry"
    )
    LaunchedEffect(Unit) { triggerAnim = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxWidth(),
            color = MatchPink40,
            trackColor = MatchPink40.copy(alpha = 0.2f)
        )

        Spacer(Modifier.height(48.dp))

        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Concluído",
            tint = MatchPink40,
            modifier = Modifier
                .size(96.dp)
                .scale(animatedScale)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Perfil configurado!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "$totalPreferencias preferências • $totalAcomodacoes acomodações",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MatchPink40.copy(alpha = 0.06f)
            )
        ) {
            Text(
                text = "Suas informações são privadas e só são usadas para melhorar sua experiência no FypMatch.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(Modifier.height(40.dp))

        FypGradientButton(
            text = "Salvar e Continuar",
            onClick = onSalvar,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
    }
}
