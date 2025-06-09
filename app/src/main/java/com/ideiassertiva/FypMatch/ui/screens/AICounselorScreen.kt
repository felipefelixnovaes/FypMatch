package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme
import com.ideiassertiva.FypMatch.ui.viewmodel.AICounselorViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICounselorScreen(
    onNavigateBack: () -> Unit = {},
    userId: String = "",
    viewModel: AICounselorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Iniciar sessão quando a tela carrega
    LaunchedEffect(userId) {
        if (userId.isNotBlank() && !uiState.hasActiveSession) {
            viewModel.startSession(userId, userSubscription = SubscriptionStatus.FREE) // TODO: Pegar do usuário logado
        }
    }
    
    // Modal de anúncio recompensa
    if (uiState.showAdRewardModal) {
        AdRewardModal(
            earnedCredits = uiState.lastEarnedCredits,
            onDismiss = { viewModel.dismissAdRewardModal() }
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
                                credits = viewModel.getUserCredits().current,
                                onWatchAd = { 
                                    if (viewModel.canWatchAd()) {
                                        viewModel.watchAdForCredits()
                                    }
                                },
                                canWatchAd = viewModel.canWatchAd()
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
                onMessageChange = viewModel::updateCurrentMessage,
                onSend = viewModel::sendMessage,
                enabled = uiState.hasActiveSession && !isLoading && viewModel.getUserCredits().current > 0,
                hasCredits = viewModel.getUserCredits().current > 0,
                onWatchAd = {
                    if (viewModel.canWatchAd()) {
                        viewModel.watchAdForCredits()
                    }
                },
                canWatchAd = viewModel.canWatchAd()
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
            tint = Color(0xFFFFD700),
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
                    tint = Color(0xFFFFD700),
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

@Preview
@Composable
private fun AICounselorScreenPreview() {
    FypMatchTheme {
        AICounselorScreen(userId = "preview")
    }
} 
