package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.theme.FypColors
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme
import com.ideiassertiva.FypMatch.ui.util.findActivity
import com.ideiassertiva.FypMatch.ui.viewmodel.AICounselorViewModel
import com.ideiassertiva.FypMatch.ui.viewmodel.AICounselorUiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICounselorScreen(
    onNavigateBack: () -> Unit = {},
    userId: String = "",
    onNavigateToComplementaryProfile: () -> Unit = {},
    viewModel: AICounselorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val activity = LocalContext.current.findActivity()

    // Iniciar sessão quando a tela carrega — subscription obtida do UserRepository no ViewModel
    LaunchedEffect(userId) {
        if (userId.isNotBlank() && !uiState.hasActiveSession) {
            viewModel.startSession(userId)
        }
    }

    AICounselorContent(
        uiState = uiState,
        currentSession = currentSession,
        isLoading = isLoading,
        userCredits = viewModel.getUserCredits().current,
        canWatchAd = viewModel.canWatchAd(),
        onNavigateBack = onNavigateBack,
        onNavigateToComplementaryProfile = onNavigateToComplementaryProfile,
        onUpdateMessage = { viewModel.updateCurrentMessage(it) },
        onSendMessage = { viewModel.sendMessage(it) },
        onWatchAd = { viewModel.watchAdForCredits(activity) },
        onDismissAdModal = { viewModel.dismissAdRewardModal() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICounselorContent(
    uiState: AICounselorUiState,
    currentSession: CounselorSession?,
    isLoading: Boolean,
    userCredits: Int,
    canWatchAd: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToComplementaryProfile: () -> Unit = {},
    onUpdateMessage: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onWatchAd: () -> Unit,
    onDismissAdModal: () -> Unit
) {
    val listState = rememberLazyListState()

    // Modal de anúncio recompensa
    if (uiState.showAdRewardModal) {
        AdRewardModal(
            earnedCredits = uiState.lastEarnedCredits,
            onDismiss = onDismissAdModal
        )
    }

    // Auto-scroll para a última mensagem
    LaunchedEffect(currentSession?.messages?.size) {
        currentSession?.let { session ->
            if (session.messages.isNotEmpty()) {
                delay(100)
                listState.animateScrollToItem(session.messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Conselheiro IA")
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Relacionamentos",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CreditsDisplay(
                                credits = userCredits,
                                onWatchAd = onWatchAd,
                                canWatchAd = canWatchAd
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Acesso ao Perfil Complementar via IA — sempre visível, sem custo de créditos
                item {
                    ComplementaryProfileEntryCard(onClick = onNavigateToComplementaryProfile)
                }

                currentSession?.messages?.let { messages ->
                    items(messages) { message ->
                        MessageBubble(message = message)
                    }
                }

                if (isLoading) {
                    item { TypingIndicator() }
                }
            }

            MessageInput(
                message = uiState.currentMessage,
                onMessageChange = onUpdateMessage,
                onSend = onSendMessage,
                enabled = uiState.hasActiveSession && !isLoading && userCredits > 0,
                hasCredits = userCredits > 0,
                onWatchAd = onWatchAd,
                canWatchAd = canWatchAd
            )
        }
    }

    // Exibir erro se houver
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Mostrar snackbar ou dialog de erro
        }
    }
}

@Composable
private fun ComplementaryProfileEntryCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .background(FypColors.BrandGradientDiagonal)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Turbine seu perfil com sua IA",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Gere um perfil complementar na IA que você já usa. Grátis, sem gastar créditos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun MessageBubble(message: CounselorMessage) {
    val isUser = message.sender == MessageSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(6.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(6.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Digitando...")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSend: (String) -> Unit,
    enabled: Boolean,
    hasCredits: Boolean = true,
    onWatchAd: () -> Unit = {},
    canWatchAd: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(if (hasCredits) "Digite sua mensagem..." else "Sem créditos - assista um anúncio")
            },
            enabled = enabled,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = { if (message.isNotBlank() && hasCredits) onSend(message) }
            ),
            supportingText = if (!hasCredits && canWatchAd) {
                { Text("Assista um anúncio para ganhar 3 créditos", color = MaterialTheme.colorScheme.primary) }
            } else null
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (hasCredits) {
            FloatingActionButton(
                onClick = { if (message.isNotBlank()) onSend(message) },
                modifier = Modifier.size(48.dp)
            ) {
                                            Icon(Icons.AutoMirrored.Filled.Send, "Enviar")
            }
        } else if (canWatchAd) {
            FloatingActionButton(
                onClick = onWatchAd,
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.PlayArrow, "Assistir Anúncio")
            }
        } else {
            FloatingActionButton(
                onClick = { },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(Icons.Default.Close, "Sem créditos")
            }
        }
    }
}

@Composable
private fun CreditsDisplay(
    credits: Int,
    onWatchAd: () -> Unit,
    canWatchAd: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Créditos",
            tint = FypColors.Gold,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = credits.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (credits == 0 && canWatchAd) {
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = onWatchAd,
                modifier = Modifier.height(24.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Assistir anúncio",
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Anúncio",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun AdRewardModal(
    earnedCredits: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = FypColors.Gold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Créditos Ganhos!")
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Você ganhou $earnedCredits créditos",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Agora você pode enviar mais mensagens para o conselheiro de relacionamentos!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Continuar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun AICounselorScreenPreview() {
    FypMatchTheme {
        AICounselorContent(
            uiState = AICounselorUiState(
                hasActiveSession = true,
                currentMessage = "Olá, preciso de ajuda."
            ),
            currentSession = CounselorSession(
                messages = listOf(
                    CounselorMessage(content = "Olá! Como posso te ajudar hoje?", sender = MessageSender.AI_COUNSELOR),
                    CounselorMessage(content = "Estou com dificuldades em iniciar conversas.", sender = MessageSender.USER)
                )
            ),
            isLoading = false,
            userCredits = 3,
            canWatchAd = true,
            onNavigateBack = {},
            onNavigateToComplementaryProfile = {},
            onUpdateMessage = {},
            onSendMessage = {},
            onWatchAd = {},
            onDismissAdModal = {}
        )
    }
}
