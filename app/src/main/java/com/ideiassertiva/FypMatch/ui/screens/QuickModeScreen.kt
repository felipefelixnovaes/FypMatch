package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ideiassertiva.FypMatch.data.repository.*
import com.ideiassertiva.FypMatch.ui.components.FypGradientButton
import com.ideiassertiva.FypMatch.ui.theme.MatchPink40
import com.ideiassertiva.FypMatch.ui.theme.MatchPurple40
import kotlinx.coroutines.launch
import java.util.Date

// ─────────────────────────────────────────────────────────────────────────────
// CONSTANTES TIPI-10
// ─────────────────────────────────────────────────────────────────────────────

private val tipiItems = listOf(
    Pair("Extrovertido(a) e entusiasmado(a)", false),
    Pair("Crítico(a) e conflituoso(a)", true),          // reverso
    Pair("Confiável e autodisciplinado(a)", false),
    Pair("Ansioso(a) e facilmente perturbado(a)", false),
    Pair("Aberto(a) a novas experiências, curioso(a)", false),
    Pair("Reservado(a) e quieto(a)", true),              // reverso
    Pair("Simpático(a) e afetuoso(a)", false),
    Pair("Desorganizado(a) e descuidado(a)", true),      // reverso
    Pair("Calmo(a) e emocionalmente estável", true),     // reverso
    Pair("Convencional e pouco criativo(a)", true)       // reverso
)

private val valoresOpcoes = listOf(
    "🔒 Segurança", "🕊️ Liberdade", "👨‍👩‍👧 Família", "🏆 Realização",
    "🌿 Tradição", "🎉 Hedonismo", "💙 Benevolência", "🌍 Universalismo",
    "💎 Poder", "📏 Conformidade"
)

private val dealBreakerOpcoes = listOf(
    "🚬 Fumar", "🍺 Beber excessivamente", "👶 Não quer filhos",
    "👶 Quer filhos obrigatório", "⛪ Religiões incompatíveis",
    "🗳️ Politicamente oposto", "💞 Relacionamento aberto",
    "👊 Violência", "💔 Infidelidade", "⚡ Ritmos muito diferentes"
)

// ─────────────────────────────────────────────────────────────────────────────
// TELA PRINCIPAL — QUICKMODESCREEN
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickModeScreen(
    onNavigateBack: () -> Unit,
    onComplete: () -> Unit,
    userId: String,
    modifier: Modifier = Modifier
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val repo = remember { QuestionnaireRepository() }

    // Estados das etapas
    val tipiValues = rememberSaveable { mutableStateListOf(*Array(10) { 4f }) }
    val valoresSelecionados = rememberSaveable { mutableStateListOf<String>() }
    var commConflictStyle by rememberSaveable { mutableStateOf("") }
    var commMessagingFreq by rememberSaveable { mutableStateOf("") }
    var commAbsenceReaction by rememberSaveable { mutableStateOf("") }
    var commConvDepth by rememberSaveable { mutableStateOf("") }
    var commConflictMedium by rememberSaveable { mutableStateOf("") }
    var routWeekendStyle by rememberSaveable { mutableStateOf("") }
    var routPlanningStyle by rememberSaveable { mutableStateOf("") }
    var routEnergySource by rememberSaveable { mutableStateOf("") }
    var routWorkLife by rememberSaveable { mutableStateOf("") }
    var routHomeNoise by rememberSaveable { mutableStateOf("") }
    val dealBreakers = rememberSaveable { mutableStateListOf<String>() }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val totalSteps = 5
    val progress = (step + 1).toFloat() / totalSteps.toFloat()

    val stepTitles = listOf(
        "Personalidade", "Valores", "Comunicação", "Rotina", "Deal-breakers"
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Modo Rápido",
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Barra de progresso ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = MatchPink40,
                    trackColor = MatchPink40.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Etapa ${step + 1} de $totalSteps — ${stepTitles[step]}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Conteúdo animado ───────────────────────────────────────────
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "questionnaire_step",
                modifier = Modifier.weight(1f)
            ) { currentStep ->
                when (currentStep) {
                    0 -> BigFiveStep(
                        values = tipiValues,
                        onValueChange = { idx, v -> tipiValues[idx] = v }
                    )
                    1 -> ValuesStep(
                        selecionados = valoresSelecionados,
                        onToggle = { valor ->
                            if (valoresSelecionados.contains(valor)) {
                                valoresSelecionados.remove(valor)
                            } else if (valoresSelecionados.size < 3) {
                                valoresSelecionados.add(valor)
                            }
                        }
                    )
                    2 -> CommunicationStep(
                        conflictStyle = commConflictStyle,
                        messagingFreq = commMessagingFreq,
                        absenceReaction = commAbsenceReaction,
                        convDepth = commConvDepth,
                        conflictMedium = commConflictMedium,
                        onConflictStyleChange = { commConflictStyle = it },
                        onMessagingFreqChange = { commMessagingFreq = it },
                        onAbsenceReactionChange = { commAbsenceReaction = it },
                        onConvDepthChange = { commConvDepth = it },
                        onConflictMediumChange = { commConflictMedium = it }
                    )
                    3 -> RoutineStep(
                        weekendStyle = routWeekendStyle,
                        planningStyle = routPlanningStyle,
                        energySource = routEnergySource,
                        workLifeBalance = routWorkLife,
                        homeNoise = routHomeNoise,
                        onWeekendStyleChange = { routWeekendStyle = it },
                        onPlanningStyleChange = { routPlanningStyle = it },
                        onEnergySourceChange = { routEnergySource = it },
                        onWorkLifeBalanceChange = { routWorkLife = it },
                        onHomeNoiseChange = { routHomeNoise = it }
                    )
                    4 -> DealBreakersStep(
                        selecionados = dealBreakers,
                        onToggle = { item ->
                            if (dealBreakers.contains(item)) dealBreakers.remove(item)
                            else dealBreakers.add(item)
                        }
                    )
                }
            }

            // ── Botões de navegação ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { step-- },
                        border = BorderStroke(1.5.dp, MatchPink40),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MatchPink40
                        )
                    ) {
                        Text("← Voltar")
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                FypGradientButton(
                    text = if (step < totalSteps - 1) "Próximo →" else "Concluir ✓",
                    isLoading = isLoading,
                    onClick = {
                        if (step < totalSteps - 1) {
                            step++
                        } else {
                            scope.launch {
                                isLoading = true
                                val bigFive = buildBigFiveResult(tipiValues)
                                val questionnaire = UserQuestionnaire(
                                    bigFive = bigFive,
                                    values = if (valoresSelecionados.isNotEmpty())
                                        ValuesResult(topValues = valoresSelecionados.toList())
                                    else null,
                                    communication = if (commConflictStyle.isNotEmpty())
                                        CommunicationResult(
                                            conflictStyle = commConflictStyle,
                                            messagingFreq = commMessagingFreq,
                                            absenceReaction = commAbsenceReaction,
                                            convDepth = commConvDepth,
                                            conflictMedium = commConflictMedium
                                        )
                                    else null,
                                    routine = if (routWeekendStyle.isNotEmpty())
                                        RoutineResult(
                                            weekendStyle = routWeekendStyle,
                                            planningStyle = routPlanningStyle,
                                            energySource = routEnergySource,
                                            workLifeBalance = routWorkLife,
                                            homeNoise = routHomeNoise
                                        )
                                    else null,
                                    dealBreakers = dealBreakers.toSet(),
                                    completedAt = Date()
                                )
                                repo.saveQuestionnaire(userId, questionnaire)
                                isLoading = false
                                onComplete()
                            }
                        }
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STEP 0 — PERSONALIDADE (TIPI-10)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BigFiveStep(
    values: List<Float>,
    onValueChange: (Int, Float) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MatchPink40.copy(alpha = 0.07f)
                )
            ) {
                Text(
                    text = "Para cada afirmação, indique o quanto ela descreve você",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        itemsIndexed(tipiItems) { idx, (texto, _) ->
            val respondido = values[idx] != 4f
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (respondido)
                        MatchPink40.copy(alpha = 0.06f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = if (respondido) BorderStroke(1.dp, MatchPink40.copy(alpha = 0.4f)) else null
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${idx + 1}. $texto",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (respondido) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Slider(
                        value = values[idx],
                        onValueChange = { onValueChange(idx, it) },
                        valueRange = 1f..7f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = MatchPink40,
                            activeTrackColor = MatchPink40
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Discordo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${values[idx].toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MatchPink40,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Concordo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/** Calcula BigFiveResult a partir das 10 respostas TIPI */
private fun buildBigFiveResult(tipiValues: List<Float>): BigFiveResult {
    fun score(a: Int, b: Int): Float {
        val vA = tipiValues[a - 1]
        val vB = 8f - tipiValues[b - 1]  // reverso
        return (vA + vB) / 2f
    }
    return BigFiveResult(
        extraversion     = score(1, 6),
        agreeableness    = score(7, 2),
        conscientiousness = score(3, 8),
        neuroticism      = score(4, 9),
        openness         = score(5, 10)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// STEP 1 — VALORES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ValuesStep(
    selecionados: List<String>,
    onToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Selecione seus 3 valores mais importantes (em ordem)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(
            text = "${selecionados.size}/3 selecionados",
            style = MaterialTheme.typography.labelMedium,
            color = if (selecionados.size == 3) MatchPink40 else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(valoresOpcoes) { _, valor ->
                val ordemIdx = selecionados.indexOf(valor)
                val selecionado = ordemIdx >= 0
                val habilitado = selecionado || selecionados.size < 3

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = habilitado) { onToggle(valor) },
                    border = if (selecionado)
                        BorderStroke(2.dp, MatchPink40)
                    else
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selecionado)
                            MatchPink40.copy(alpha = 0.1f)
                        else if (!habilitado)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = valor,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (!habilitado && !selecionado)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        if (selecionado) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(MatchPink40, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${ordemIdx + 1}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STEP 2 — COMUNICAÇÃO
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CommunicationStep(
    conflictStyle: String,
    messagingFreq: String,
    absenceReaction: String,
    convDepth: String,
    conflictMedium: String,
    onConflictStyleChange: (String) -> Unit,
    onMessagingFreqChange: (String) -> Unit,
    onAbsenceReactionChange: (String) -> Unit,
    onConvDepthChange: (String) -> Unit,
    onConflictMediumChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        QuizCard(
            pergunta = "Quando estou chateado(a)...",
            opcoes = listOf("Na hora" to "onTheSpot", "Esperar" to "waitCool", "Me afastar" to "withdraw"),
            selecionado = conflictStyle,
            onSelect = onConflictStyleChange
        )
        QuizCard(
            pergunta = "Mensagens ao longo do dia...",
            opcoes = listOf("Frequência" to "frequent", "Depende" to "contextual", "Prefiro espaço" to "spacious"),
            selecionado = messagingFreq,
            onSelect = onMessagingFreqChange
        )
        QuizCard(
            pergunta = "Quando alguém some por horas...",
            opcoes = listOf("Normal" to "normal", "Um pouco ansioso(a)" to "slightlyAnxious", "Me preocupo" to "veryAnxious"),
            selecionado = absenceReaction,
            onSelect = onAbsenceReactionChange
        )
        QuizCard(
            pergunta = "Prefiro conversas...",
            opcoes = listOf("Diretas" to "direct", "Profundas" to "deep", "Misto" to "mixed"),
            selecionado = convDepth,
            onSelect = onConvDepthChange
        )
        QuizCard(
            pergunta = "Conflito prefiro resolver...",
            opcoes = listOf("Na hora" to "verbal", "Depois" to "deferred", "Por escrito" to "written"),
            selecionado = conflictMedium,
            onSelect = onConflictMediumChange
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STEP 3 — ROTINA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RoutineStep(
    weekendStyle: String,
    planningStyle: String,
    energySource: String,
    workLifeBalance: String,
    homeNoise: String,
    onWeekendStyleChange: (String) -> Unit,
    onPlanningStyleChange: (String) -> Unit,
    onEnergySourceChange: (String) -> Unit,
    onWorkLifeBalanceChange: (String) -> Unit,
    onHomeNoiseChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        QuizCard(
            pergunta = "Fim de semana ideal",
            opcoes = listOf("Em casa 🏠" to "homebody", "Equilíbrio ⚖️" to "balanced", "Explorar 🗺️" to "explorer"),
            selecionado = weekendStyle,
            onSelect = onWeekendStyleChange
        )
        QuizCard(
            pergunta = "Sou mais...",
            opcoes = listOf("Rotina 📅" to "routine", "Espontâneo(a) 🎲" to "spontaneous", "Depende" to "mixed"),
            selecionado = planningStyle,
            onSelect = onPlanningStyleChange
        )
        QuizCard(
            pergunta = "Me recarrego...",
            opcoes = listOf("Sozinho(a) 🤫" to "introvert", "Com pessoas 🎊" to "extrovert"),
            selecionado = energySource,
            onSelect = onEnergySourceChange
        )
        QuizCard(
            pergunta = "Trabalho e vida...",
            opcoes = listOf("Alta ambição 🚀" to "ambitious", "Equilíbrio ⚖️" to "balanced", "Qualidade de vida 🌻" to "leisureFirst"),
            selecionado = workLifeBalance,
            onSelect = onWorkLifeBalanceChange
        )
        QuizCard(
            pergunta = "Em casa preciso de...",
            opcoes = listOf("Silêncio 🤫" to "quiet", "Som/movimento 🎵" to "someNoise", "Qualquer" to "anyEnv"),
            selecionado = homeNoise,
            onSelect = onHomeNoiseChange
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STEP 4 — DEAL-BREAKERS
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DealBreakersStep(
    selecionados: List<String>,
    onToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "O que seria inaceitável para você? (selecione quantos quiser)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            dealBreakerOpcoes.forEach { item ->
                val selecionado = selecionados.contains(item)
                FilterChip(
                    selected = selecionado,
                    onClick = { onToggle(item) },
                    label = { Text(item, style = MaterialTheme.typography.bodySmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD32F2F).copy(alpha = 0.15f),
                        selectedLabelColor = Color(0xFFD32F2F),
                        selectedLeadingIconColor = Color(0xFFD32F2F)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selecionado,
                        selectedBorderColor = Color(0xFFD32F2F),
                        selectedBorderWidth = 1.5.dp
                    )
                )
            }
        }

        if (selecionados.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFD32F2F).copy(alpha = 0.07f)
                )
            ) {
                Text(
                    text = "${selecionados.size} item(s) selecionado(s) como inaceitável(is)",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD32F2F)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPONENTE AUXILIAR — QUIZ CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuizCard(
    pergunta: String,
    opcoes: List<Pair<String, String>>,  // (label, valor)
    selecionado: String,
    onSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = pergunta,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                opcoes.forEach { (label, valor) ->
                    val ativo = selecionado == valor
                    FilterChip(
                        selected = ativo,
                        onClick = { onSelect(valor) },
                        label = {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MatchPink40.copy(alpha = 0.15f),
                            selectedLabelColor = MatchPink40
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = ativo,
                            selectedBorderColor = MatchPink40,
                            selectedBorderWidth = 1.5.dp
                        )
                    )
                }
            }
        }
    }
}
