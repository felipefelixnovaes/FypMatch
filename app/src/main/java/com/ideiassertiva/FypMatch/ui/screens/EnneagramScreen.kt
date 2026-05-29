package com.ideiassertiva.FypMatch.ui.screens

// ─────────────────────────────────────────────────────────────────────────────
// EnneagramScreen.kt
// FypMatch — Sprint 7a · Android Squad
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
import com.ideiassertiva.FypMatch.model.EnneagramResult
import com.ideiassertiva.FypMatch.model.EnneagramType
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

private val FypPink   = Color(0xFFE91E63)
private val FypPurple = Color(0xFF9C27B0)
private val FypGradient = Brush.horizontalGradient(listOf(FypPink, FypPurple))

// ─────────────────────────────────────────────────────────────────────────────
// 27 PARES DE PERGUNTAS — alinhados com EnneagramResult.itemMap
// índice = posição na itemMap do Architecture Squad (QuestionnaireModels.kt)
// ─────────────────────────────────────────────────────────────────────────────

private data class EnneagramItem(val optionA: String, val optionB: String)

private val enneagramItems = listOf(
    // 0  ONE  vs THREE
    EnneagramItem(
        "Valorizo a integridade acima do sucesso",
        "Valorizo o sucesso e ser reconhecido"
    ),
    // 1  ONE  vs FOUR
    EnneagramItem(
        "Sigo princípios mesmo que seja difícil",
        "Expresso o que sinto com profundidade"
    ),
    // 2  ONE  vs FIVE
    EnneagramItem(
        "Tenho padrões altos para tudo que faço",
        "Prefiro entender antes de agir"
    ),
    // 3  ONE  vs SIX
    EnneagramItem(
        "Corrijo erros, nos outros e em mim",
        "Penso muito nos riscos antes de decidir"
    ),
    // 4  ONE  vs SEVEN
    EnneagramItem(
        "Evito excessos e mantenho a disciplina",
        "Busco variedade e novas experiências"
    ),
    // 5  ONE  vs EIGHT
    EnneagramItem(
        "Prefiro fazer as coisas do jeito certo",
        "Prefiro agir com força e decisão"
    ),
    // 6  TWO  vs FOUR
    EnneagramItem(
        "Sinto satisfação em ser necessário(a)",
        "Sinto que sou diferente da maioria"
    ),
    // 7  TWO  vs FIVE
    EnneagramItem(
        "Me aproximo das pessoas com afeto",
        "Me afasto para pensar com clareza"
    ),
    // 8  TWO  vs SIX
    EnneagramItem(
        "Dou muito de mim nas relações",
        "Sou leal e sigo quem confio"
    ),
    // 9  TWO  vs SEVEN
    EnneagramItem(
        "Me preocupo com o bem-estar dos outros",
        "Busco diversão e fuga da rotina"
    ),
    // 10 TWO  vs EIGHT
    EnneagramItem(
        "Cuido dos outros com carinho",
        "Protejo quem amo com firmeza"
    ),
    // 11 TWO  vs NINE
    EnneagramItem(
        "Antecipo o que os outros precisam",
        "Evito conflitos e mantenho a paz"
    ),
    // 12 THREE vs FIVE
    EnneagramItem(
        "Gosto de ser visto(a) como competente",
        "Prefiro ser reconhecido(a) pela inteligência"
    ),
    // 13 THREE vs SIX
    EnneagramItem(
        "Foco em resultados e conquistas",
        "Foco em segurança e lealdade"
    ),
    // 14 THREE vs SEVEN
    EnneagramItem(
        "Trabalho duro para alcançar meus objetivos",
        "Busco diversão e fuga da rotina"
    ),
    // 15 THREE vs EIGHT
    EnneagramItem(
        "Mostro meu valor pelo que conquisto",
        "Mostro meu valor pela minha força"
    ),
    // 16 THREE vs NINE
    EnneagramItem(
        "Adapto minha imagem ao contexto",
        "Me adapto para manter harmonia"
    ),
    // 17 FOUR  vs SIX
    EnneagramItem(
        "Prefiro autenticidade a segurança",
        "Prefiro segurança a correr riscos"
    ),
    // 18 FOUR  vs SEVEN
    EnneagramItem(
        "Mergulho em emoções profundas",
        "Fujo de emoções pesadas com leveza"
    ),
    // 19 FOUR  vs EIGHT
    EnneagramItem(
        "Me expresso mesmo sendo vulnerável",
        "Me expresso mesmo sendo confrontador(a)"
    ),
    // 20 FOUR  vs NINE
    EnneagramItem(
        "Aceito minha singularidade",
        "Aceito as diferenças dos outros"
    ),
    // 21 FIVE  vs SEVEN
    EnneagramItem(
        "Preciso de silêncio para recarregar",
        "Preciso de estímulo para recarregar"
    ),
    // 22 FIVE  vs EIGHT
    EnneagramItem(
        "Ganho poder pelo conhecimento",
        "Ganho poder pela ação direta"
    ),
    // 23 FIVE  vs NINE
    EnneagramItem(
        "Me retiro para processar o mundo",
        "Me retiro para manter a paz interna"
    ),
    // 24 SIX   vs EIGHT
    EnneagramItem(
        "Busco segurança nos vínculos",
        "Busco segurança na força própria"
    ),
    // 25 SIX   vs NINE
    EnneagramItem(
        "Me preocupo com o que pode dar errado",
        "Confio que as coisas vão se resolver"
    ),
    // 26 SEVEN vs NINE
    EnneagramItem(
        "Fujo da dor com alegria e leveza",
        "Aceito a dor e encontro paz interior"
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// VIEWMODEL
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class EnneagramViewModel @Inject constructor(
    private val repository: QuestionnaireRepository
) : ViewModel() {

    sealed class UiState {
        data class Content(
            val responses: List<Boolean?> = List(27) { null },
            val isSaving: Boolean = false,
            val isComplete: Boolean = false,
            val result: EnneagramResult? = null,
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
                val enneagramResult = EnneagramResult(boolResponses)
                val questionnaire = SelfKnowledgeQuestionnaire(
                    userId = userId,
                    completedAt = System.currentTimeMillis(),
                    enneagram = enneagramResult
                )
                repository.saveSelfKnowledge(questionnaire)
                _uiState.value = current.copy(
                    isSaving = false,
                    isComplete = true,
                    result = enneagramResult,
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
fun EnneagramScreen(
    onNavigateBack: () -> Unit,
    onComplete: () -> Unit,
    userId: String,
    viewModel: EnneagramViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val content = uiState as? EnneagramViewModel.UiState.Content ?: return

    // Exibe folha de resultado quando completo
    if (content.isComplete && content.result != null) {
        EnneagramResultSheet(
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
                        text = "Eneagrama",
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
                    progress = { viewModel.answeredCount / 27f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = FypPink,
                    trackColor = FypPink.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${viewModel.answeredCount}/27 respondidas",
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
                            containerColor = FypPink.copy(alpha = 0.07f)
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

                itemsIndexed(enneagramItems) { index, item ->
                    EnneagramItemCard(
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
                            brush = if (enabled) FypGradient
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
                            text = "Ver meu tipo ✨",
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
fun EnneagramItemCard(
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
                EnneagramOptionButton(
                    text = optionA,
                    isSelected = response == true,
                    onClick = onChooseA,
                    modifier = Modifier.weight(1f)
                )
                EnneagramOptionButton(
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
private fun EnneagramOptionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = if (isSelected) FypGradient
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
fun EnneagramResultSheet(
    result: EnneagramResult,
    onContinue: () -> Unit
) {
    val dominantType = result.dominantType()
    val topThree = result.topThree()
    val scores = result.scores()
    val maxScore = scores.values.maxOrNull()?.coerceAtLeast(1) ?: 1

    // Animação de escala do emoji ao entrar
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "emoji_scale"
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
                text = dominantType.emoji,
                fontSize = 80.sp,
                modifier = Modifier.scale(scale)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = dominantType.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dominantType.shortDescription,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Divisória
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Seus 3 tipos dominantes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Barras de progresso do top 3
            topThree.forEachIndexed { rank, type ->
                val typeScore = scores[type] ?: 0
                val progress = typeScore.toFloat() / maxScore.toFloat()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${type.emoji} ${type.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(160.dp),
                        fontWeight = if (rank == 0) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (rank == 0) FypPink else FypPurple.copy(alpha = 0.6f + rank * 0.1f),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$typeScore",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    .background(FypGradient)
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
