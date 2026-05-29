// DeepModeScreen.kt
// FypMatch — Wizard de 5 passos do Modo Profundo
// Android Squad — Sprint 6

package com.ideiassertiva.FypMatch.ui.screens
// NOTE: Este arquivo está em ideiassertivas/ (com s) mas o package é ideiassertiva (sem s)
// Mover para o diretório correto: com/ideiassertiva/FypMatch/ui/screens/

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.QuestionnaireRepository
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.components.FypGradientButton
import com.ideiassertiva.FypMatch.ui.theme.MatchPink40
import com.ideiassertiva.FypMatch.ui.theme.MatchPurple40
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// GRADIENTE DO DESIGN SYSTEM
// ─────────────────────────────────────────────────────────────────────────────

private val fypGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0))
)

// ─────────────────────────────────────────────────────────────────────────────
// VIEWMODEL
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class DeepModeViewModel @Inject constructor(
    private val repository: QuestionnaireRepository
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Content(
            val currentStep: Int = 0,
            val totalSteps: Int = 5,
            // IPIP-20: escala 1–5, default 3
            val ipip20Responses: List<Int> = List(20) { 3 },
            // PVQ-21: escala 1–6, default 3
            val pvq21Responses: List<Int> = List(21) { 3 },
            // ECR-RS: escala 1–7, default 4
            val ecrrResponses: List<Int> = List(12) { 4 },
            // Conflito
            val conflictResolutionStyle: ConflictResolutionStyle = ConflictResolutionStyle.COOLING_OFF,
            val emotionalExpression: EmotionalExpression = EmotionalExpression.MODERATE,
            val repairBehavior: RepairBehavior = RepairBehavior.GRADUAL,
            val silencePeriod: SilencePeriod = SilencePeriod.HOURS,
            val apologyStyle: ApologyStyle = ApologyStyle.BOTH,
            val feedbackTolerance: FeedbackTolerance = FeedbackTolerance.CONTEXTUAL,
            // Projeto de Vida
            val childrenDesire: ChildrenDesire = ChildrenDesire.UNDECIDED,
            val locationFlexibility: LocationFlexibility = LocationFlexibility.OPEN_SAME_CITY,
            val careerPriority: CareerPriority = CareerPriority.BALANCED,
            val financialApproach: FinancialApproach = FinancialApproach.BALANCED,
            val spiritualityRole: SpiritualityRole = SpiritualityRole.PERSONAL,
            val isSaving: Boolean = false,
            val isComplete: Boolean = false,
            val errorMessage: String? = null
        ) : UiState()

        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Content())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // ── Navegação de passos ───────────────────────────────────────────────────

    fun nextStep() {
        val state = _uiState.value as? UiState.Content ?: return
        if (state.currentStep < state.totalSteps - 1) {
            _uiState.update { (it as UiState.Content).copy(currentStep = it.currentStep + 1) }
        }
    }

    fun previousStep() {
        val state = _uiState.value as? UiState.Content ?: return
        if (state.currentStep > 0) {
            _uiState.update { (it as UiState.Content).copy(currentStep = it.currentStep - 1) }
        }
    }

    // ── IPIP-20 ───────────────────────────────────────────────────────────────

    fun updateIPIP20(index: Int, value: Int) {
        _uiState.update { state ->
            if (state !is UiState.Content) return@update state
            val updated = state.ipip20Responses.toMutableList().also { it[index] = value }
            state.copy(ipip20Responses = updated)
        }
    }

    // ── PVQ-21 ────────────────────────────────────────────────────────────────

    fun updatePVQ21(index: Int, value: Int) {
        _uiState.update { state ->
            if (state !is UiState.Content) return@update state
            val updated = state.pvq21Responses.toMutableList().also { it[index] = value }
            state.copy(pvq21Responses = updated)
        }
    }

    // ── ECR-RS ────────────────────────────────────────────────────────────────

    fun updateECRRS(index: Int, value: Int) {
        _uiState.update { state ->
            if (state !is UiState.Content) return@update state
            val updated = state.ecrrResponses.toMutableList().also { it[index] = value }
            state.copy(ecrrResponses = updated)
        }
    }

    // ── Conflito ──────────────────────────────────────────────────────────────

    fun updateConflictResolutionStyle(v: ConflictResolutionStyle) =
        _uiState.update { (it as? UiState.Content)?.copy(conflictResolutionStyle = v) ?: it }

    fun updateEmotionalExpression(v: EmotionalExpression) =
        _uiState.update { (it as? UiState.Content)?.copy(emotionalExpression = v) ?: it }

    fun updateRepairBehavior(v: RepairBehavior) =
        _uiState.update { (it as? UiState.Content)?.copy(repairBehavior = v) ?: it }

    fun updateSilencePeriod(v: SilencePeriod) =
        _uiState.update { (it as? UiState.Content)?.copy(silencePeriod = v) ?: it }

    fun updateApologyStyle(v: ApologyStyle) =
        _uiState.update { (it as? UiState.Content)?.copy(apologyStyle = v) ?: it }

    fun updateFeedbackTolerance(v: FeedbackTolerance) =
        _uiState.update { (it as? UiState.Content)?.copy(feedbackTolerance = v) ?: it }

    // ── Projeto de Vida ───────────────────────────────────────────────────────

    fun updateChildrenDesire(v: ChildrenDesire) =
        _uiState.update { (it as? UiState.Content)?.copy(childrenDesire = v) ?: it }

    fun updateLocationFlexibility(v: LocationFlexibility) =
        _uiState.update { (it as? UiState.Content)?.copy(locationFlexibility = v) ?: it }

    fun updateCareerPriority(v: CareerPriority) =
        _uiState.update { (it as? UiState.Content)?.copy(careerPriority = v) ?: it }

    fun updateFinancialApproach(v: FinancialApproach) =
        _uiState.update { (it as? UiState.Content)?.copy(financialApproach = v) ?: it }

    fun updateSpiritualityRole(v: SpiritualityRole) =
        _uiState.update { (it as? UiState.Content)?.copy(spiritualityRole = v) ?: it }

    // ── Salvar ────────────────────────────────────────────────────────────────

    fun save(userId: String) {
        val state = _uiState.value as? UiState.Content ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { (it as UiState.Content).copy(isSaving = true, errorMessage = null) }
            try {
                val questionnaire = DeepModeQuestionnaire(
                    userId = userId,
                    ipip20 = IPIP20Result(state.ipip20Responses),
                    pvq21 = PVQ21Result(state.pvq21Responses),
                    ecrrs = ECRRSResult(state.ecrrResponses),
                    conflictDeep = DeepConflictResult(
                        resolutionStyle = state.conflictResolutionStyle,
                        emotionalExpression = state.emotionalExpression,
                        repairBehavior = state.repairBehavior,
                        silencePeriod = state.silencePeriod,
                        apologyStyle = state.apologyStyle,
                        feedbackTolerance = state.feedbackTolerance
                    ),
                    lifeProject = LifeProjectResult(
                        childrenDesire = state.childrenDesire,
                        locationFlexibility = state.locationFlexibility,
                        careerPriority = state.careerPriority,
                        financialApproach = state.financialApproach,
                        spiritualityRole = state.spiritualityRole
                    )
                )
                repository.saveDeepMode(questionnaire)
                _uiState.update {
                    (it as UiState.Content).copy(isSaving = false, isComplete = true)
                }
            } catch (e: Exception) {
                _uiState.update {
                    (it as UiState.Content).copy(
                        isSaving = false,
                        errorMessage = "Erro ao salvar: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TELA PRINCIPAL — DEEPMODESCREEN
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepModeScreen(
    onNavigateBack: () -> Unit,
    onComplete: () -> Unit,
    userId: String,
    viewModel: DeepModeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val state = uiState as? DeepModeViewModel.UiState.Content
        ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MatchPink40)
            }
            return
        }

    val stepTitles = listOf(
        "Personalidade", "Valores", "Estilo de Vínculo", "Conflitos", "Projeto de Vida"
    )
    val progress = (state.currentStep + 1).toFloat() / state.totalSteps.toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modo Profundo", fontWeight = FontWeight.Bold) },
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
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (!state.isComplete)
                        "Etapa ${state.currentStep + 1} de ${state.totalSteps} — ${stepTitles[state.currentStep]}"
                    else "Concluído!",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Mensagem de erro ───────────────────────────────────────────
            state.errorMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // ── Conteúdo animado ───────────────────────────────────────────
            AnimatedContent(
                targetState = if (state.isComplete) -1 else state.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "deep_mode_step",
                modifier = Modifier.weight(1f)
            ) { currentStep ->
                when (currentStep) {
                    -1 -> DeepModeCompleteContent(
                        ecrrsResult = ECRRSResult(state.ecrrResponses),
                        onComplete = onComplete
                    )
                    0 -> IPIP20Step(
                        responses = state.ipip20Responses,
                        onUpdate = { idx, v -> viewModel.updateIPIP20(idx, v) }
                    )
                    1 -> PVQ21Step(
                        responses = state.pvq21Responses,
                        onUpdate = { idx, v -> viewModel.updatePVQ21(idx, v) }
                    )
                    2 -> ECRRSStep(
                        responses = state.ecrrResponses,
                        onUpdate = { idx, v -> viewModel.updateECRRS(idx, v) }
                    )
                    3 -> ConflictStep(
                        resolutionStyle = state.conflictResolutionStyle,
                        emotionalExpression = state.emotionalExpression,
                        repairBehavior = state.repairBehavior,
                        silencePeriod = state.silencePeriod,
                        apologyStyle = state.apologyStyle,
                        feedbackTolerance = state.feedbackTolerance,
                        onResolutionStyleChange = { viewModel.updateConflictResolutionStyle(it) },
                        onEmotionalExpressionChange = { viewModel.updateEmotionalExpression(it) },
                        onRepairBehaviorChange = { viewModel.updateRepairBehavior(it) },
                        onSilencePeriodChange = { viewModel.updateSilencePeriod(it) },
                        onApologyStyleChange = { viewModel.updateApologyStyle(it) },
                        onFeedbackToleranceChange = { viewModel.updateFeedbackTolerance(it) }
                    )
                    4 -> LifeProjectStep(
                        childrenDesire = state.childrenDesire,
                        locationFlexibility = state.locationFlexibility,
                        careerPriority = state.careerPriority,
                        financialApproach = state.financialApproach,
                        spiritualityRole = state.spiritualityRole,
                        onChildrenDesireChange = { viewModel.updateChildrenDesire(it) },
                        onLocationFlexibilityChange = { viewModel.updateLocationFlexibility(it) },
                        onCareerPriorityChange = { viewModel.updateCareerPriority(it) },
                        onFinancialApproachChange = { viewModel.updateFinancialApproach(it) },
                        onSpiritualityRoleChange = { viewModel.updateSpiritualityRole(it) }
                    )
                }
            }

            // ── Botões de navegação (ocultos na tela de conclusão) ─────────
            if (!state.isComplete) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.currentStep > 0) {
                        OutlinedButton(
                            onClick = { viewModel.previousStep() },
                            border = BorderStroke(1.5.dp, MatchPink40),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MatchPink40)
                        ) {
                            Text("← Anterior")
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    FypGradientButton(
                        text = if (state.currentStep < state.totalSteps - 1) "Próximo →" else "Concluir ✓",
                        isLoading = state.isSaving,
                        onClick = {
                            if (state.currentStep < state.totalSteps - 1) {
                                viewModel.nextStep()
                            } else {
                                viewModel.save(userId)
                            }
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TELA DE CONCLUSÃO
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DeepModeCompleteContent(
    ecrrsResult: ECRRSResult,
    onComplete: () -> Unit
) {
    val attachmentStyle = ecrrsResult.attachmentStyle()
    val scale = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            1f,
            animationSpec = androidx.compose.animation.core.spring(
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(24.dp))

        // Ícone animado
        Box(
            modifier = Modifier
                .size((80 * scale.value).dp)
                .clip(CircleShape)
                .background(MatchPink40.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MatchPink40,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Modo Profundo concluído! 🎉",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Seu perfil de compatibilidade está muito mais completo agora.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // Card estilo de apego
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MatchPurple40.copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.5.dp, MatchPurple40.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seu estilo de apego",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = attachmentStyle.emoji,
                    fontSize = 40.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = attachmentStyle.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MatchPurple40
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = attachmentStyle.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Agora podemos encontrar pessoas que realmente combinam com a sua forma de se relacionar.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // Botão gradiente "Continuar"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(fypGradient)
                .clickable { onComplete() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Continuar",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PASSO 1 — IPIP-20 (Personalidade aprofundada)
// ─────────────────────────────────────────────────────────────────────────────

private val ipip20Items = listOf(
    // Extroversão (0–3)
    Triple("Sou o centro das atenções nas festas", "Extroversão", false),
    Triple("Falo pouco com desconhecidos", "Extroversão", true),
    Triple("Me sinto bem em ambientes sociais", "Extroversão", false),
    Triple("Gosto de estar rodeado de pessoas", "Extroversão", false),
    // Amabilidade (4–7)
    Triple("Me importo com os outros", "Amabilidade", false),
    Triple("Frequentemente insulto as pessoas", "Amabilidade", true),
    Triple("Sinto empatia pelas emoções dos outros", "Amabilidade", false),
    Triple("Faço as pessoas se sentirem bem-vindas", "Amabilidade", false),
    // Conscienciosidade (8–11)
    Triple("Sempre estou preparado(a)", "Conscienciosidade", false),
    Triple("Deixo uma bagunça por onde passo", "Conscienciosidade", true),
    Triple("Presto atenção nos detalhes", "Conscienciosidade", false),
    Triple("Faço meu trabalho imediatamente", "Conscienciosidade", false),
    // Neuroticismo (12–15)
    Triple("Fico estressado(a) facilmente", "Neuroticismo", false),
    Triple("Me preocupo com as coisas", "Neuroticismo", false),
    Triple("Mudo de humor com facilidade", "Neuroticismo", false),
    Triple("Me irrito facilmente", "Neuroticismo", false),
    // Abertura (16–19)
    Triple("Tenho uma imaginação viva", "Abertura", false),
    Triple("Tenho dificuldade de entender ideias abstratas", "Abertura", true),
    Triple("Uso um vocabulário rico", "Abertura", false),
    Triple("Não gosto de poesia", "Abertura", true)
)

private val ipip20FactorColors = mapOf(
    "Extroversão" to Color(0xFFE91E63),
    "Amabilidade" to Color(0xFF9C27B0),
    "Conscienciosidade" to Color(0xFF3F51B5),
    "Neuroticismo" to Color(0xFFFF5722),
    "Abertura" to Color(0xFF009688)
)

@Composable
private fun IPIP20Step(
    responses: List<Int>,
    onUpdate: (Int, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Personalidade aprofundada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MatchPink40.copy(alpha = 0.07f)
                    )
                ) {
                    Text(
                        text = "Para cada afirmação, indique o quanto ela descreve você.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        var lastFactor = ""
        ipip20Items.forEachIndexed { idx, (text, factor, _) ->
            if (factor != lastFactor) {
                lastFactor = factor
                val color = ipip20FactorColors[factor] ?: MatchPink40
                item(key = "header_$factor") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (idx == 0) 0.dp else 8.dp),
                        color = color.copy(alpha = 0.13f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = factor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            val capturedIdx = idx
            val capturedText = text
            item(key = "item_$capturedIdx") {
                DiscreteSliderRow(
                    label = "${capturedIdx + 1}. $capturedText",
                    range = 1..5,
                    value = responses[capturedIdx],
                    onValueChange = { onUpdate(capturedIdx, it) },
                    minLabel = "Discordo",
                    maxLabel = "Concordo"
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PASSO 2 — PVQ-21 (Valores)
// ─────────────────────────────────────────────────────────────────────────────

private val pvq21Items = listOf(
    // Conformidade (0, 10)
    "Pensa que as pessoas devem fazer o que lhes é dito. Acha que é importante seguir regras sempre.",
    // Tradição (1, 11)
    "Acha importante fazer as coisas da forma como aprendeu com a família. Respeita tradições.",
    // Benevolência (2, 12)
    "É muito importante para ela/ele ajudar as pessoas ao seu redor. Quer cuidar do bem-estar delas.",
    // Universalismo (3, 13, 19)
    "Acha importante que todos sejam tratados com igualdade. Quer justiça para todos.",
    // Autodireção (4, 14)
    "Pensa que é importante ter suas próprias ideias. Gosta de ser criativo(a) e original.",
    // Estimulação (5, 15)
    "Pensa que é importante fazer muitas coisas diferentes. Sempre está em busca de novidades.",
    // Hedonismo (6, 20)
    "Aproveitar a vida ao máximo é importante para ela/ele. Quer se divertir.",
    // Realização (7, 16)
    "Ser muito bem-sucedido(a) é importante. Gosta de impressionar as pessoas.",
    // Poder (8, 17)
    "É importante para ela/ele ser rica(o). Quer ter muito dinheiro e coisas caras.",
    // Segurança (9, 18)
    "É importante viver em um ambiente seguro. Evita qualquer coisa que pareça perigosa.",
    // Conformidade (10)
    "Acredita que as pessoas devem respeitar as regras o tempo todo, mesmo quando ninguém está olhando.",
    // Tradição (11)
    "Seguir costumes e tradições é importante. Mantém práticas religiosas e familiares.",
    // Benevolência (12)
    "Responder às necessidades dos outros é importante. Tenta apoiar quem conhece.",
    // Universalismo (13)
    "Acha que é importante se preocupar com a natureza. Cuidar do meio ambiente.",
    // Autodireção (14)
    "Tomar suas próprias decisões é muito importante. Gosta de liberdade e não depender dos outros.",
    // Estimulação (15)
    "Gosta de surpresas e sempre fica em busca de novas atividades para fazer.",
    // Realização (16)
    "É muito ambicioso(a). Quer mostrar o quanto é capaz e ser reconhecido(a).",
    // Poder (17)
    "É importante para ela/ele dizer o que fazer. Quer que as pessoas façam o que manda.",
    // Segurança (18)
    "É importante para ela/ele que o governo garanta sua segurança contra todas as ameaças.",
    // Universalismo (19)
    "Acredita que pessoas de todo o mundo devem viver em harmonia. Promover a paz.",
    // Hedonismo (20)
    "Buscar prazer é importante para ela/ele. Aproveitar ao máximo tudo que a vida oferece."
)

@Composable
private fun PVQ21Step(
    responses: List<Int>,
    onUpdate: (Int, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Seus valores mais profundos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MatchPurple40.copy(alpha = 0.07f)
                    )
                ) {
                    Text(
                        text = "O quanto essa pessoa se parece com você?\n1 = Muito parecido  •  6 = Nada parecido",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        itemsIndexed(pvq21Items) { idx, text ->
            DiscreteSliderRow(
                label = "${idx + 1}. $text",
                range = 1..6,
                value = responses[idx],
                onValueChange = { onUpdate(idx, it) },
                minLabel = "Muito parecido",
                maxLabel = "Nada parecido",
                activeColor = MatchPurple40
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PASSO 3 — ECR-RS (Estilo de Vínculo)
// ─────────────────────────────────────────────────────────────────────────────

private val ecrrItems = listOf(
    // Ansiedade (0–5)
    "Tenho medo de perder o amor do meu parceiro(a).",
    "Me preocupo muito com meus relacionamentos.",
    "Frequentemente me preocupo que meu parceiro(a) não queira ficar comigo.",
    "Quero ficar muito próximo(a) do meu parceiro(a), mas acabo afastando-o(a).",
    "Me preocupo com a possibilidade de ser abandonado(a).",
    "Minha insegurança nos relacionamentos me causa muita ansiedade.",
    // Evitação (6–11)
    "Prefiro não me apegar emocionalmente ao meu parceiro(a).",
    "Fico desconfortável ao me sentir próximo(a) de outra pessoa.",
    "Acho difícil permitir que meu parceiro(a) dependa de mim.",
    "Prefiro não compartilhar meus sentimentos profundos com meu parceiro(a).",
    "Me sinto desconfortável quando me abro com meu parceiro(a).",
    "Prefiro manter distância emocional mesmo em relacionamentos."
)

@Composable
private fun ECRRSStep(
    responses: List<Int>,
    onUpdate: (Int, Int) -> Unit
) {
    val ecrrResult = ECRRSResult(responses)
    val attachmentStyle = ecrrResult.attachmentStyle()
    val allAnswered = responses.none { it == 0 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Seu estilo de vínculo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MatchPink40.copy(alpha = 0.07f)
                    )
                ) {
                    Text(
                        text = "Como você geralmente se sente em relacionamentos românticos.\n1 = Discordo fortemente  •  7 = Concordo fortemente",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Seção Ansiedade
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE91E63).copy(alpha = 0.10f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Ansiedade de Apego",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFE91E63),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        itemsIndexed(ecrrItems.subList(0, 6)) { idx, text ->
            DiscreteSliderRow(
                label = "${idx + 1}. $text",
                range = 1..7,
                value = responses[idx],
                onValueChange = { onUpdate(idx, it) },
                minLabel = "Discordo",
                maxLabel = "Concordo",
                activeColor = Color(0xFFE91E63)
            )
        }

        // Seção Evitação
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = Color(0xFF9C27B0).copy(alpha = 0.10f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Conforto com Proximidade",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF9C27B0),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        itemsIndexed(ecrrItems.subList(6, 12)) { relIdx, text ->
            val idx = relIdx + 6
            DiscreteSliderRow(
                label = "${idx + 1}. $text",
                range = 1..7,
                value = responses[idx],
                onValueChange = { onUpdate(idx, it) },
                minLabel = "Discordo",
                maxLabel = "Concordo",
                activeColor = Color(0xFF9C27B0)
            )
        }

        // Preview do estilo de apego
        item {
            AnimatedVisibility(visible = true) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MatchPurple40.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, MatchPurple40.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = attachmentStyle.emoji, fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Estilo de apego atual: ${attachmentStyle.displayName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MatchPurple40
                            )
                            Text(
                                text = attachmentStyle.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PASSO 4 — CONFLITO
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ConflictStep(
    resolutionStyle: ConflictResolutionStyle,
    emotionalExpression: EmotionalExpression,
    repairBehavior: RepairBehavior,
    silencePeriod: SilencePeriod,
    apologyStyle: ApologyStyle,
    feedbackTolerance: FeedbackTolerance,
    onResolutionStyleChange: (ConflictResolutionStyle) -> Unit,
    onEmotionalExpressionChange: (EmotionalExpression) -> Unit,
    onRepairBehaviorChange: (RepairBehavior) -> Unit,
    onSilencePeriodChange: (SilencePeriod) -> Unit,
    onApologyStyleChange: (ApologyStyle) -> Unit,
    onFeedbackToleranceChange: (FeedbackTolerance) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        Text(
            text = "Como você lida com conflitos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        DeepQuizCard(
            pergunta = "Como você costuma lidar com conflitos?",
            opcoes = ConflictResolutionStyle.entries.map { it.displayName to it },
            selecionado = resolutionStyle,
            onSelect = onResolutionStyleChange
        )
        DeepQuizCard(
            pergunta = "Como você expressa emoções difíceis?",
            opcoes = EmotionalExpression.entries.map { it.displayName to it },
            selecionado = emotionalExpression,
            onSelect = onEmotionalExpressionChange
        )
        DeepQuizCard(
            pergunta = "Depois de uma briga, você...",
            opcoes = RepairBehavior.entries.map { it.displayName to it },
            selecionado = repairBehavior,
            onSelect = onRepairBehaviorChange
        )
        DeepQuizCard(
            pergunta = "Quanto tempo você precisa de espaço após um conflito?",
            opcoes = SilencePeriod.entries.map { it.displayName to it },
            selecionado = silencePeriod,
            onSelect = onSilencePeriodChange
        )
        DeepQuizCard(
            pergunta = "Quando você erra, prefere...",
            opcoes = ApologyStyle.entries.map { it.displayName to it },
            selecionado = apologyStyle,
            onSelect = onApologyStyleChange
        )
        DeepQuizCard(
            pergunta = "Como você recebe críticas?",
            opcoes = FeedbackTolerance.entries.map { it.displayName to it },
            selecionado = feedbackTolerance,
            onSelect = onFeedbackToleranceChange
        )

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PASSO 5 — PROJETO DE VIDA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LifeProjectStep(
    childrenDesire: ChildrenDesire,
    locationFlexibility: LocationFlexibility,
    careerPriority: CareerPriority,
    financialApproach: FinancialApproach,
    spiritualityRole: SpiritualityRole,
    onChildrenDesireChange: (ChildrenDesire) -> Unit,
    onLocationFlexibilityChange: (LocationFlexibility) -> Unit,
    onCareerPriorityChange: (CareerPriority) -> Unit,
    onFinancialApproachChange: (FinancialApproach) -> Unit,
    onSpiritualityRoleChange: (SpiritualityRole) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        Text(
            text = "Projeto de vida compartilhado",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        DeepQuizCard(
            pergunta = "Sobre ter filhos:",
            opcoes = ChildrenDesire.entries.map { it.displayName to it },
            selecionado = childrenDesire,
            onSelect = onChildrenDesireChange
        )
        DeepQuizCard(
            pergunta = "Flexibilidade de morar em outro lugar:",
            opcoes = LocationFlexibility.entries.map { it.displayName to it },
            selecionado = locationFlexibility,
            onSelect = onLocationFlexibilityChange
        )
        DeepQuizCard(
            pergunta = "Carreira na sua vida:",
            opcoes = CareerPriority.entries.map { it.displayName to it },
            selecionado = careerPriority,
            onSelect = onCareerPriorityChange
        )
        DeepQuizCard(
            pergunta = "Relação com dinheiro:",
            opcoes = FinancialApproach.entries.map { it.displayName to it },
            selecionado = financialApproach,
            onSelect = onFinancialApproachChange
        )
        DeepQuizCard(
            pergunta = "Espiritualidade e religião:",
            opcoes = SpiritualityRole.entries.map { it.displayName to it },
            selecionado = spiritualityRole,
            onSelect = onSpiritualityRoleChange
        )

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPONENTES AUXILIARES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Linha de botões circulares discretos para escala numérica.
 * Selecionado: gradiente fypGradient. Não selecionado: cinza claro.
 */
@Composable
fun DiscreteSliderRow(
    label: String,
    range: IntRange,
    value: Int,
    onValueChange: (Int) -> Unit,
    minLabel: String = "",
    maxLabel: String = "",
    activeColor: Color = MatchPink40
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (value in range)
                activeColor.copy(alpha = 0.05f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = if (value in range)
            BorderStroke(1.dp, activeColor.copy(alpha = 0.3f))
        else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (value in range) FontWeight.SemiBold else FontWeight.Normal
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                range.forEach { v ->
                    val selected = v == value
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected)
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0))
                                    )
                                else
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )
                            )
                            .border(
                                width = if (selected) 0.dp else 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable { onValueChange(v) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = v.toString(),
                            color = if (selected) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            if (minLabel.isNotEmpty() || maxLabel.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = minLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = maxLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Quiz card genérico para opções enum — exibe as opções em coluna.
 */
@Composable
private fun <T> DeepQuizCard(
    pergunta: String,
    opcoes: List<Pair<String, T>>,
    selecionado: T,
    onSelect: (T) -> Unit
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
            Spacer(Modifier.height(8.dp))
            opcoes.forEach { (label, valor) ->
                val ativo = selecionado == valor
                FilterChip(
                    selected = ativo,
                    onClick = { onSelect(valor) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
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
