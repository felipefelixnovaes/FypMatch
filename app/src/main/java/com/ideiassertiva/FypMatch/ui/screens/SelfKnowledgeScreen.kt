package com.ideiassertiva.FypMatch.ui.screens

// ─────────────────────────────────────────────────────────────────────────────
// SelfKnowledgeScreen.kt
// FypMatch — Sprint 7b · Android Squad
// ─────────────────────────────────────────────────────────────────────────────

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

private val SkFypPink   = Color(0xFFE91E63)
private val SkFypPurple = Color(0xFF9C27B0)
private val SkFypGradient = Brush.horizontalGradient(listOf(SkFypPink, SkFypPurple))
private val SkFypLinearGradient = Brush.linearGradient(listOf(SkFypPink, SkFypPurple))

// ─────────────────────────────────────────────────────────────────────────────
// VIEWMODEL
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class SelfKnowledgeViewModel @Inject constructor(
    private val repository: QuestionnaireRepository
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Content(val questionnaire: SelfKnowledgeQuestionnaire? = null) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Loading
            try {
                val questionnaire = repository.loadSelfKnowledge(userId)
                _uiState.value = UiState.Content(questionnaire)
            } catch (e: Exception) {
                _uiState.value = UiState.Content(null)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TELA PRINCIPAL
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfKnowledgeScreen(
    onNavigateBack: () -> Unit,
    onStartEnneagram: (String) -> Unit,
    onStartLoveLanguage: (String) -> Unit,
    onStartArchetype: (String) -> Unit,
    userId: String,
    viewModel: SelfKnowledgeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.load(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Autoconhecimento",
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
        when (uiState) {
            is SelfKnowledgeViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SkFypPink)
                }
            }

            is SelfKnowledgeViewModel.UiState.Content -> {
                val content = uiState as SelfKnowledgeViewModel.UiState.Content
                val questionnaire = content.questionnaire

                val isEnneagramComplete = questionnaire?.enneagram != null
                val isLoveLanguageComplete = questionnaire?.loveLanguage != null
                val isArchetypeComplete = questionnaire?.archetype != null
                val allComplete = isEnneagramComplete && isLoveLanguageComplete && isArchetypeComplete

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Header ────────────────────────────────────────────
                    item {
                        Text(
                            text = "🧠 Seu Perfil de Autoconhecimento",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // ── Subtítulo ─────────────────────────────────────────
                    item {
                        Text(
                            text = "Descubra seu tipo, sua linguagem do cuidado e seu arquétipo para encontrar conexões mais profundas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // ── Eneagrama ─────────────────────────────────────────
                    item {
                        val enneagramEmoji = questionnaire?.enneagram?.dominantType()?.emoji
                        val enneagramName = questionnaire?.enneagram?.dominantType()?.displayName
                        val enneagramDesc = questionnaire?.enneagram?.dominantType()?.shortDescription

                        SelfKnowledgeModuleCard(
                            sectionTitle = "Eneagrama",
                            emoji = enneagramEmoji,
                            resultName = enneagramName,
                            description = enneagramDesc,
                            isComplete = isEnneagramComplete,
                            onAction = { onStartEnneagram(userId) }
                        )
                    }

                    // ── Linguagem do Cuidado ───────────────────────────────
                    item {
                        val llEmoji = questionnaire?.loveLanguage?.primaryLanguage()?.emoji
                        val llName = questionnaire?.loveLanguage?.primaryLanguage()?.displayName
                        val llDesc = questionnaire?.loveLanguage?.primaryLanguage()?.description

                        SelfKnowledgeModuleCard(
                            sectionTitle = "Linguagem do Cuidado",
                            emoji = llEmoji,
                            resultName = llName,
                            description = llDesc,
                            isComplete = isLoveLanguageComplete,
                            onAction = { onStartLoveLanguage(userId) }
                        )
                    }

                    // ── Arquétipo ─────────────────────────────────────────
                    item {
                        val archetypeEmoji = questionnaire?.archetype?.dominantArchetype()?.emoji
                        val archetypeName = questionnaire?.archetype?.dominantArchetype()?.displayName

                        SelfKnowledgeModuleCard(
                            sectionTitle = "Arquétipo",
                            emoji = archetypeEmoji,
                            resultName = archetypeName,
                            description = null,
                            isComplete = isArchetypeComplete,
                            onAction = { onStartArchetype(userId) }
                        )
                    }

                    // ── CombinationCard (só quando os 3 completos) ─────────
                    item {
                        AnimatedVisibility(
                            visible = allComplete,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                        ) {
                            if (allComplete && questionnaire != null) {
                                CombinationCard(questionnaire = questionnaire)
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MODULE CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SelfKnowledgeModuleCard(
    sectionTitle: String,
    emoji: String?,
    resultName: String?,
    description: String?,
    isComplete: Boolean,
    onAction: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = sectionTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = SkFypPurple,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isComplete && emoji != null && resultName != null) {
                    // Estado completo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = resultName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (description != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onAction,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Refazer")
                    }
                } else {
                    // Estado incompleto
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🔒",
                            fontSize = 40.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Ainda não descoberto",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SkFypGradient)
                            .clickable(onClick = onAction),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Descobrir →",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMBINATION CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CombinationCard(questionnaire: SelfKnowledgeQuestionnaire) {
    val enneagramName = questionnaire.enneagram?.dominantType()?.displayName ?: ""
    val loveLanguageName = questionnaire.loveLanguage?.primaryLanguage()?.displayName ?: ""
    val archetypeName = questionnaire.archetype?.dominantArchetype()?.displayName ?: ""

    val enneagramEmoji = questionnaire.enneagram?.dominantType()?.emoji ?: ""
    val loveLanguageEmoji = questionnaire.loveLanguage?.primaryLanguage()?.emoji ?: ""
    val archetypeEmoji = questionnaire.archetype?.dominantArchetype()?.emoji ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SkFypLinearGradient)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row com os 3 emojis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = enneagramEmoji, fontSize = 40.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = loveLanguageEmoji, fontSize = 40.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = archetypeEmoji, fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Frase de combinação
            Text(
                text = "Um(a) $enneagramName que se sente amado(a) com $loveLanguageName e carrega o espírito de $archetypeName.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
