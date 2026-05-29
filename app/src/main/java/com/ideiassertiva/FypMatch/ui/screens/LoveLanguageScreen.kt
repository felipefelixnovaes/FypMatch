package com.ideiassertiva.FypMatch.ui.screens

// ─────────────────────────────────────────────────────────────────────────────
// LoveLanguageScreen.kt
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
import com.ideiassertiva.FypMatch.model.LoveLanguage
import com.ideiassertiva.FypMatch.model.LoveLanguageResult
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

private val LlFypPink   = Color(0xFFE91E63)
private val LlFypPurple = Color(0xFF9C27B0)
private val LlFypGradient = Brush.horizontalGradient(listOf(LlFypPink, LlFypPurple))

// ─────────────────────────────────────────────────────────────────────────────
// 30 PARES DE PERGUNTAS — alinhados com LoveLanguageResult.itemMap
// itemMap: (0,1),(0,2),(0,3),(0,4),(1,2),(1,3),(1,4),(2,3),(2,4),(3,4) × 3 grupos
// 0=WORDS_OF_AFFIRMATION, 1=ACTS_OF_SERVICE, 2=RECEIVING_GIFTS,
// 3=QUALITY_TIME, 4=PHYSICAL_TOUCH
// ─────────────────────────────────────────────────────────────────────────────

private data class LoveLanguageItem(val optionA: String, val optionB: String)

private val loveLanguageItems = listOf(
    // Grupo 1 (índices 0-9)
    // 0: WORDS vs ACTS
    LoveLanguageItem(
        "Quando alguém me elogia sinceramente",
        "Quando alguém faz algo por mim sem eu pedir"
    ),
    // 1: WORDS vs GIFTS
    LoveLanguageItem(
        "Ouvir 'eu te amo' e palavras carinhosas",
        "Receber um presente pensado especialmente para mim"
    ),
    // 2: WORDS vs QUALITY_TIME
    LoveLanguageItem(
        "Uma conversa profunda e atenciosa",
        "Passar tempo de qualidade juntos, sem distrações"
    ),
    // 3: WORDS vs PHYSICAL_TOUCH
    LoveLanguageItem(
        "Mensagens e palavras de incentivo",
        "Um abraço longo ou carinho físico"
    ),
    // 4: ACTS vs GIFTS
    LoveLanguageItem(
        "Quando o outro resolve algo que me preocupava",
        "Ganhar algo que mostra que a pessoa pensa em mim"
    ),
    // 5: ACTS vs QUALITY_TIME
    LoveLanguageItem(
        "Alguém que ajuda nas tarefas do dia a dia",
        "Alguém que desliga o celular e fica presente"
    ),
    // 6: ACTS vs PHYSICAL_TOUCH
    LoveLanguageItem(
        "Quando o outro cuida de detalhes práticos",
        "Segurar a mão ou encostar no ombro"
    ),
    // 7: GIFTS vs QUALITY_TIME
    LoveLanguageItem(
        "Um mimo surpresa, mesmo pequeno",
        "Um programa especial planejado para nós dois"
    ),
    // 8: GIFTS vs PHYSICAL_TOUCH
    LoveLanguageItem(
        "Um presente que mostra que a pessoa me conhece",
        "Proximidade física e contato constante"
    ),
    // 9: QUALITY_TIME vs PHYSICAL_TOUCH
    LoveLanguageItem(
        "Uma tarde inteira só nós dois, sem interrupções",
        "Trocar carinhos e estar próximo(a) fisicamente"
    ),

    // Grupo 2 (índices 10-19)
    // 10: WORDS vs ACTS
    LoveLanguageItem(
        "Ser elogiado(a) na frente de outras pessoas",
        "Alguém que faz esforço para facilitar minha vida"
    ),
    // 11: WORDS vs GIFTS
    LoveLanguageItem(
        "Uma carta ou mensagem escrita com carinho",
        "Um presente que mostra atenção aos meus gostos"
    ),
    // 12: WORDS vs QUALITY_TIME
    LoveLanguageItem(
        "Alguém que me diz o quanto eu sou importante",
        "Um fim de semana juntos sem agenda lotada"
    ),
    // 13: WORDS vs PHYSICAL_TOUCH
    LoveLanguageItem(
        "Palavras de apoio quando estou mal",
        "Um colo ou presença física quando estou mal"
    ),
    // 14: ACTS vs GIFTS
    LoveLanguageItem(
        "Alguém que organiza algo complicado para mim",
        "Receber algo que eu queria há tempo"
    ),
    // 15: ACTS vs QUALITY_TIME
    LoveLanguageItem(
        "Quando o outro se antecipa e já resolveu algo",
        "Quando o outro cancela outros planos para estar comigo"
    ),
    // 16: ACTS vs PHYSICAL_TOUCH
    LoveLanguageItem(
        "Ser surpreendido(a) com uma tarefa já feita",
        "Massagem ou toque reconfortante após um dia difícil"
    ),
    // 17: GIFTS vs QUALITY_TIME
    LoveLanguageItem(
        "Um presente simbólico de uma data especial",
        "Fazer juntos algo que sempre quis experienciar"
    ),
    // 18: GIFTS vs PHYSICAL_TOUCH
    LoveLanguageItem(
        "Receber flores ou algo inesperado",
        "Ficar abraçado(a) enquanto assistimos algo"
    ),
    // 19: QUALITY_TIME vs PHYSICAL_TOUCH
    LoveLanguageItem(
        "Jantarmos juntos sem celular nenhum na mesa",
        "Dormir abraçados ou ficar de mãos dadas"
    ),

    // Grupo 3 (índices 20-29)
    // 20: WORDS vs ACTS
    LoveLanguageItem(
        "'Estou orgulhoso(a) de você' dito com sinceridade",
        "Alguém que faz o jantar quando chego cansado(a)"
    ),
    // 21: WORDS vs GIFTS
    LoveLanguageItem(
        "Uma dedicatória escrita num livro ou cartão",
        "Um presente que custou tempo e cuidado"
    ),
    // 22: WORDS vs QUALITY_TIME
    LoveLanguageItem(
        "Ser reconhecido(a) verbalmente pelos meus esforços",
        "Passeios ou atividades que planejamos juntos"
    ),
    // 23: WORDS vs PHYSICAL_TOUCH
    LoveLanguageItem(
        "Mensagem de bom dia com palavras carinhosas",
        "Um beijo surpresa ou abraço pelo avesso do dia"
    ),
    // 24: ACTS vs GIFTS
    LoveLanguageItem(
        "Alguém que cuida de mim quando estou doente",
        "Receber algo que lembra uma memória nossa"
    ),
    // 25: ACTS vs QUALITY_TIME
    LoveLanguageItem(
        "Quando o outro lembra de fazer algo que me ajuda",
        "Uma viagem curta ou experiência só para nós"
    ),
    // 26: ACTS vs PHYSICAL_TOUCH
    LoveLanguageItem(
        "Alguém que trata bem as pessoas que eu amo",
        "Carinho espontâneo sem motivo especial"
    ),
    // 27: GIFTS vs QUALITY_TIME
    LoveLanguageItem(
        "Algo feito à mão com dedicação",
        "Um ritual semanal só nosso"
    ),
    // 28: GIFTS vs PHYSICAL_TOUCH
    LoveLanguageItem(
        "Presente surpresa num dia comum",
        "Dormir colado(a) ou ficar juntinhos no sofá"
    ),
    // 29: QUALITY_TIME vs PHYSICAL_TOUCH
    LoveLanguageItem(
        "Prioridade: me ver pessoalmente",
        "Prioridade: me tocar com carinho quando está perto"
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// VIEWMODEL
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class LoveLanguageViewModel @Inject constructor(
    private val repository: QuestionnaireRepository
) : ViewModel() {

    sealed class UiState {
        data class Content(
            val responses: List<Boolean?> = List(30) { null },
            val isSaving: Boolean = false,
            val isComplete: Boolean = false,
            val result: LoveLanguageResult? = null,
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
                val loveLanguageResult = LoveLanguageResult(boolResponses)
                val questionnaire = SelfKnowledgeQuestionnaire(
                    userId = userId,
                    completedAt = System.currentTimeMillis(),
                    loveLanguage = loveLanguageResult
                )
                repository.saveSelfKnowledge(questionnaire)
                _uiState.value = current.copy(
                    isSaving = false,
                    isComplete = true,
                    result = loveLanguageResult,
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
fun LoveLanguageScreen(
    onNavigateBack: () -> Unit,
    onComplete: () -> Unit,
    userId: String,
    viewModel: LoveLanguageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val content = uiState as? LoveLanguageViewModel.UiState.Content ?: return

    // Exibe folha de resultado quando completo
    if (content.isComplete && content.result != null) {
        LoveLanguageResultSheet(
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
                        text = "Linguagem do Cuidado",
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
                    progress = { viewModel.answeredCount / 30f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = LlFypPink,
                    trackColor = LlFypPink.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${viewModel.answeredCount}/30 respondidas",
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
                            containerColor = LlFypPink.copy(alpha = 0.07f)
                        )
                    ) {
                        Text(
                            text = "Para cada par, escolha o que mais te faz sentir amado(a)",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                itemsIndexed(loveLanguageItems) { index, item ->
                    LoveLanguageItemCard(
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
                            brush = if (enabled) LlFypGradient
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
                            text = "Ver minha linguagem 💬",
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
fun LoveLanguageItemCard(
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
                LoveLanguageOptionButton(
                    text = optionA,
                    isSelected = response == true,
                    onClick = onChooseA,
                    modifier = Modifier.weight(1f)
                )
                LoveLanguageOptionButton(
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
private fun LoveLanguageOptionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = if (isSelected) LlFypGradient
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
fun LoveLanguageResultSheet(
    result: LoveLanguageResult,
    onContinue: () -> Unit
) {
    val primaryLanguage = result.primaryLanguage()
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
                text = primaryLanguage.emoji,
                fontSize = 80.sp,
                modifier = Modifier.scale(scale)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = primaryLanguage.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = primaryLanguage.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Score por linguagem",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Barras de progresso para cada linguagem
            LoveLanguage.values().forEach { language ->
                val langScore = scores[language] ?: 0
                val progress = langScore.toFloat() / maxScore.toFloat()
                val isPrimary = language == primaryLanguage

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${language.emoji} ${language.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(180.dp),
                        fontWeight = if (isPrimary) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isPrimary) LlFypPink else LlFypPurple.copy(alpha = 0.6f),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$langScore",
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
                    .background(LlFypGradient)
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
