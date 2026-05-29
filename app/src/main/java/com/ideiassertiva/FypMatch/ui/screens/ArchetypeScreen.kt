package com.ideiassertiva.FypMatch.ui.screens

// ─────────────────────────────────────────────────────────────────────────────
// ArchetypeScreen.kt
// FypMatch — Sprint 7b · Android Squad
// ─────────────────────────────────────────────────────────────────────────────

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.ideiassertiva.FypMatch.model.ArchetypeResult
import com.ideiassertiva.FypMatch.model.FypArchetype
import com.ideiassertiva.FypMatch.model.SelfKnowledgeQuestionnaire
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// CORES DO DESIGN SYSTEM
// ─────────────────────────────────────────────────────────────────────────────

private val AtFypPink   = Color(0xFFE91E63)
private val AtFypPurple = Color(0xFF9C27B0)
private val AtFypGradient = Brush.horizontalGradient(listOf(AtFypPink, AtFypPurple))

// ─────────────────────────────────────────────────────────────────────────────
// 24 PARES DE PERGUNTAS — alinhados com ArchetypeResult.itemMap
// itemMap (0-based):
// 0=EXPLORER,1=CREATOR,2=CAREGIVER,3=RULER,4=SAGE,5=HERO
// 6=REBEL,7=LOVER,8=JESTER,9=INNOCENT,10=MAGICIAN,11=EVERYMAN
//  0:(0,1)  1:(0,2)  2:(0,6)  3:(0,9)  4:(1,3)  5:(1,7)
//  6:(1,10) 7:(2,4)  8:(2,8)  9:(2,11) 10:(3,5) 11:(3,8)
// 12:(3,11) 13:(4,6) 14:(4,9) 15:(4,11) 16:(5,7) 17:(5,10)
// 18:(5,9) 19:(6,7) 20:(6,8) 21:(7,11) 22:(8,10) 23:(9,10)
// ─────────────────────────────────────────────────────────────────────────────

private data class ArchetypeItem(val optionA: String, val optionB: String)

private val archetypeItems = listOf(
    // 0: EXPLORER vs CREATOR
    ArchetypeItem(
        "Prefiro explorar o desconhecido",
        "Prefiro criar algo do zero"
    ),
    // 1: EXPLORER vs CAREGIVER
    ArchetypeItem(
        "Encontro sentido ao descobrir novos lugares e pessoas",
        "Encontro sentido ao cuidar de quem está ao meu redor"
    ),
    // 2: EXPLORER vs REBEL
    ArchetypeItem(
        "Busco liberdade pelo movimento e pela descoberta",
        "Busco liberdade desafiando o que está estabelecido"
    ),
    // 3: EXPLORER vs INNOCENT
    ArchetypeItem(
        "Vejo o mundo com curiosidade e desejo de explorar",
        "Vejo o mundo com esperança e fé no que é bom"
    ),
    // 4: CREATOR vs RULER
    ArchetypeItem(
        "Prefiro construir algo novo e original",
        "Prefiro organizar e liderar para criar ordem"
    ),
    // 5: CREATOR vs LOVER
    ArchetypeItem(
        "Expresso amor criando beleza e obras",
        "Expresso amor com profundidade emocional e entrega"
    ),
    // 6: CREATOR vs MAGICIAN
    ArchetypeItem(
        "Transformo o mundo pelo que crio",
        "Transformo o mundo pelo que imagino e manifesto"
    ),
    // 7: CAREGIVER vs SAGE
    ArchetypeItem(
        "Prefiro cuidar e apoiar as pessoas",
        "Prefiro ensinar e compartilhar o que sei"
    ),
    // 8: CAREGIVER vs JESTER
    ArchetypeItem(
        "Conecto com as pessoas cuidando delas",
        "Conecto com as pessoas com leveza e humor"
    ),
    // 9: CAREGIVER vs EVERYMAN
    ArchetypeItem(
        "Me importo profundamente com quem amo",
        "Me importo com a comunidade e o pertencimento"
    ),
    // 10: RULER vs HERO
    ArchetypeItem(
        "Lidero estabelecendo estrutura e regras claras",
        "Lidero enfrentando desafios com coragem"
    ),
    // 11: RULER vs JESTER
    ArchetypeItem(
        "Organizo as coisas para criar ordem e eficiência",
        "Deixo as coisas fluírem com leveza e surpresa"
    ),
    // 12: RULER vs EVERYMAN
    ArchetypeItem(
        "Ganho meu espaço pelo controle e planejamento",
        "Ganho meu espaço pela genuinidade e pertencimento"
    ),
    // 13: SAGE vs REBEL
    ArchetypeItem(
        "Busco sabedoria e verdade como forma de ser",
        "Questiono o status quo e abro novos caminhos"
    ),
    // 14: SAGE vs INNOCENT
    ArchetypeItem(
        "Reflito com profundidade sobre a vida e as verdades",
        "Aceito a vida com gratidão e simplicidade"
    ),
    // 15: SAGE vs EVERYMAN
    ArchetypeItem(
        "Valorizo o conhecimento e a busca pela compreensão",
        "Valorizo a conexão real e o senso de comunidade"
    ),
    // 16: HERO vs LOVER
    ArchetypeItem(
        "Me movo pela conquista e pela superação",
        "Me movo pela conexão e pela intensidade emocional"
    ),
    // 17: HERO vs MAGICIAN
    ArchetypeItem(
        "Supero obstáculos com força e determinação",
        "Supero obstáculos com visão e capacidade de transformar"
    ),
    // 18: HERO vs INNOCENT
    ArchetypeItem(
        "Enfrento desafios com coragem e ação direta",
        "Enfrento desafios com fé e confiança no bem"
    ),
    // 19: REBEL vs LOVER
    ArchetypeItem(
        "Me entrego ao que acredito mesmo contra o sistema",
        "Me entrego completamente às pessoas que amo"
    ),
    // 20: REBEL vs JESTER
    ArchetypeItem(
        "Transformo com provocação e ruptura",
        "Transformo com humor e surpresa"
    ),
    // 21: LOVER vs EVERYMAN
    ArchetypeItem(
        "Amo com intensidade e entrega total",
        "Amo de forma genuína e sem pretensão"
    ),
    // 22: JESTER vs MAGICIAN
    ArchetypeItem(
        "Conecto as pessoas com alegria e espontaneidade",
        "Conecto as pessoas de forma invisível e encantadora"
    ),
    // 23: INNOCENT vs MAGICIAN
    ArchetypeItem(
        "Acredito na bondade das pessoas e no bem",
        "Acredito que tudo está conectado de formas invisíveis"
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// VIEWMODEL
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class ArchetypeViewModel @Inject constructor(
    private val repository: QuestionnaireRepository
) : ViewModel() {

    sealed class UiState {
        data class Content(
            val responses: List<Boolean?> = List(24) { null },
            val isSaving: Boolean = false,
            val isComplete: Boolean = false,
            val result: ArchetypeResult? = null,
            val errorMessage: String? = null
        ) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Content())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val allAnswered: Boolean
        get() = (_uiState.value as? UiState.Content)?.responses?.all { it != null } ?: false

    val answeredCount: Int
        get() = (_uiState.value as? UiState.Content)?.responses?.count { it != null } ?: 0

    fun respond(index: Int, chooseA: Boolean) {
        val current = _uiState.value as? UiState.Content ?: return
        val updated = current.responses.toMutableList().also { it[index] = chooseA }
        _uiState.value = current.copy(responses = updated)
    }

    fun save(userId: String) {
        val current = _uiState.value as? UiState.Content ?: return
        if (!allAnswered) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = current.copy(isSaving = true)
            try {
                val boolResponses = current.responses.map { it ?: false }
                val archetypeResult = ArchetypeResult(boolResponses)
                val questionnaire = SelfKnowledgeQuestionnaire(
                    userId = userId,
                    completedAt = System.currentTimeMillis(),
                    archetype = archetypeResult
                )
                repository.saveSelfKnowledge(questionnaire)
                _uiState.value = current.copy(
                    isSaving = false,
                    isComplete = true,
                    result = archetypeResult,
                    responses = boolResponses
                )
            } catch (e: Exception) {
                _uiState.value = current.copy(
                    isSaving = false,
                    errorMessage = "Erro ao salvar: ${e.message}"
                )
            }
        }
    }

    fun dismissError() {
        val current = _uiState.value as? UiState.Content ?: return
        _uiState.value = current.copy(errorMessage = null)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TELA PRINCIPAL
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchetypeScreen(
    onNavigateBack: () -> Unit,
    onComplete: () -> Unit,
    userId: String,
    viewModel: ArchetypeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val content = uiState as? ArchetypeViewModel.UiState.Content ?: return

    // Exibe folha de resultado quando completo
    if (content.isComplete && content.result != null) {
        ArchetypeResultSheet(
            result = content.result,
            onContinue = onComplete
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Arquétipo",
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
                    progress = { viewModel.answeredCount / 24f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = AtFypPink,
                    trackColor = AtFypPink.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${viewModel.answeredCount}/24 respondidas",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Lista de perguntas ─────────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = AtFypPink.copy(alpha = 0.07f)
                        )
                    ) {
                        Text(
                            text = "Para cada par, escolha a afirmação que mais combina com você",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                itemsIndexed(archetypeItems) { index, item ->
                    ArchetypeItemCard(
                        index = index,
                        optionA = item.optionA,
                        optionB = item.optionB,
                        response = content.responses[index],
                        onChooseA = { viewModel.respond(index, true) },
                        onChooseB = { viewModel.respond(index, false) }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // ── Rodapé — botão de conclusão ────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                val enabled = viewModel.allAnswered && !content.isSaving
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            brush = if (enabled) AtFypGradient
                            else Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                )
                            )
                        )
                        .clickable(enabled = enabled) { viewModel.save(userId) },
                    contentAlignment = Alignment.Center
                ) {
                    if (content.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Descobrir meu arquétipo ✨",
                            color = if (enabled) Color.White
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Snackbar de erro
    content.errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Erro") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text("OK")
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CARD DE ITEM
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArchetypeItemCard(
    index: Int,
    optionA: String,
    optionB: String,
    response: Boolean?,
    onChooseA: () -> Unit,
    onChooseB: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Pergunta ${index + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ArchetypeOptionButton(
                    text = optionA,
                    isSelected = response == true,
                    onClick = onChooseA,
                    modifier = Modifier.weight(1f)
                )
                ArchetypeOptionButton(
                    text = optionB,
                    isSelected = response == false,
                    onClick = onChooseB,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ArchetypeOptionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = if (isSelected) AtFypGradient
                else Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FOLHA DE RESULTADO
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ArchetypeResultSheet(
    result: ArchetypeResult,
    onContinue: () -> Unit
) {
    val dominantArchetype = result.dominantArchetype()
    val topThree = result.topThree()

    // Animação de escala do emoji ao entrar
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "archetype_emoji_scale"
    )

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Emoji animado
            Text(
                text = dominantArchetype.emoji,
                fontSize = 80.sp,
                modifier = Modifier.scale(scale)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = dominantArchetype.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Seus top 3 arquétipos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Top 3 como FilterChips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topThree.forEach { archetype ->
                    FilterChip(
                        selected = archetype == dominantArchetype,
                        onClick = {},
                        label = {
                            Text(
                                text = "${archetype.emoji} ${archetype.displayName}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AtFypPink.copy(alpha = 0.15f),
                            selectedLabelColor = AtFypPink
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botão continuar com gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(AtFypGradient)
                    .clickable(onClick = onContinue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Continuar",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
