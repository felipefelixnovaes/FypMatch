package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.viewmodel.EnhancedChatViewModel
import com.ideiassertiva.FypMatch.ui.viewmodel.ConnectionState
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedChatScreen(
    conversationId: String,
    currentUserId: String,
    onBackClick: () -> Unit,
    useFirebase: Boolean = false, // Toggle to test Firebase vs Mock
    viewModel: EnhancedChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showAttachments by remember { mutableStateOf(false) }
    var showAISuggestions by remember { mutableStateOf(false) }
    
    LaunchedEffect(conversationId) {
        viewModel.loadConversation(conversationId, currentUserId, useFirebase)
    }
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Enhanced Header with connection status
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = uiState.otherUser?.name ?: "Chat",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Real-time status indicators
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Connection status indicator
                        ConnectionStatusIndicator(uiState.connectionState)
                        
                        when {
                            uiState.isOtherUserTyping -> {
                                Text(
                                    "digitando...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            uiState.isOtherUserOnline -> {
                                Text(
                                    "online",
                                    fontSize = 12.sp,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            else -> {
                                val lastSeen = uiState.conversation?.getLastSeenFormatted(currentUserId)
                                if (!lastSeen.isNullOrBlank()) {
                                    Text(
                                        lastSeen,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        
                        // Firebase indicator
                        if (useFirebase) {
                            Icon(
                                Icons.Filled.Cloud,
                                contentDescription = "Firebase",
                                tint = when (uiState.connectionState) {
                                    ConnectionState.CONNECTED -> Color(0xFF4CAF50)
                                    ConnectionState.CONNECTING -> MaterialTheme.colorScheme.primary
                                    ConnectionState.ERROR -> Color(0xFFFF5722)
                                    ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onSurface.copy(0.4f)
                                },
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            },
            navigationIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                    
                    uiState.otherUser?.let { user ->
                        AsyncImage(
                            model = user.profileImageUrl,
                            contentDescription = user.name,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                }
            },
            actions = {
                // Connection retry button for Firebase
                if (useFirebase && uiState.connectionState == ConnectionState.ERROR) {
                    IconButton(onClick = { viewModel.retryConnection() }) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Reconectar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Error handling with retry option
        uiState.error?.let { error ->
            ErrorMessage(
                error = error,
                onRetry = if (useFirebase) { { viewModel.retryConnection() } } else null,
                onDismiss = { viewModel.clearError() }
            )
        }
        
        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Text(
                        if (useFirebase) "Conectando ao Firebase..." else "Carregando conversa...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.messages) { message ->
                EnhancedMessageBubble(
                    message = message,
                    isFromCurrentUser = message.senderId == currentUserId,
                    otherUser = uiState.otherUser,
                    onReactionClick = { emoji ->
                        viewModel.addReaction(message.id, emoji)
                    },
                    showDeliveryStatus = useFirebase // Only show for Firebase messages
                )
            }
            
            // Typing indicator
            if (uiState.isOtherUserTyping) {
                item {
                    TypingIndicatorBubble(
                        userName = uiState.otherUser?.name ?: "Usuário"
                    )
                }
            }
        }
        
        // Enhanced Input Area with real-time features
        EnhancedMessageInput(
            currentMessage = uiState.currentMessage,
            onMessageChange = { viewModel.updateMessage(it) },
            onSendMessage = { viewModel.sendMessage() },
            onSendLocation = { lat, lng, address ->
                viewModel.sendLocation(lat, lng, address)
            },
            onSendGif = { gifUrl ->
                viewModel.sendGif(gifUrl)
            },
            isTyping = uiState.isTyping,
            connectionState = uiState.connectionState
        )
    }
}

@Composable
fun ConnectionStatusIndicator(connectionState: ConnectionState) {
    val color = when (connectionState) {
        ConnectionState.CONNECTED -> Color(0xFF4CAF50)
        ConnectionState.CONNECTING -> MaterialTheme.colorScheme.primary
        ConnectionState.ERROR -> Color(0xFFFF5722)
        ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    }
    
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape)
    )
}

@Composable
fun ErrorMessage(
    error: String,
    onRetry: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Error,
                contentDescription = null,
                tint = Color(0xFFE57373)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = error,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text("TENTAR NOVAMENTE")
                }
            }
            
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Fechar")
            }
        }
    }
}

@Composable
fun EnhancedMessageBubble(
    message: Message,
    isFromCurrentUser: Boolean,
    otherUser: User?,
    onReactionClick: (String) -> Unit,
    showDeliveryStatus: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    ) {
        // Message content
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isFromCurrentUser) 20.dp else 4.dp,
                bottomEnd = if (isFromCurrentUser) 4.dp else 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromCurrentUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.getDisplayContent(),
                    color = if (isFromCurrentUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Timestamp and status
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = message.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFromCurrentUser) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                    
                    // Delivery status for sent messages
                    if (isFromCurrentUser && showDeliveryStatus) {
                        Spacer(modifier = Modifier.width(4.dp))
                        when (message.status) {
                            MessageStatus.SENDING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.dp,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                            }
                            MessageStatus.SENT -> {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Enviado",
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                            }
                            MessageStatus.DELIVERED -> {
                                Icon(
                                    Icons.Filled.Done,
                                    contentDescription = "Entregue",
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                            }
                            MessageStatus.READ -> {
                                Icon(
                                    Icons.Filled.DoneAll,
                                    contentDescription = "Lido",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Reactions
        if (message.reactions.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(message.reactions.groupBy { it.emoji }) { (emoji, reactions) ->
                    Surface(
                        modifier = Modifier.clickable { onReactionClick(emoji) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(emoji, fontSize = 14.sp)
                            if (reactions.size > 1) {
                                Text(
                                    text = reactions.size.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble(userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$userName está digitando",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                
                // Animated dots
                repeat(3) { index ->
                    val alpha by animateFloatAsState(
                        targetValue = if ((System.currentTimeMillis() / 500) % 3 == index.toLong()) 1f else 0.3f,
                        label = "typing_dot_$index"
                    )
                    Text(
                        text = "●",
                        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                        modifier = Modifier.padding(horizontal = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedMessageInput(
    currentMessage: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSendLocation: (Double, Double, String?) -> Unit,
    onSendGif: (String) -> Unit,
    isTyping: Boolean,
    connectionState: ConnectionState
) {
    var showAttachments by remember { mutableStateOf(false) }
    
    Column {
        // Connection status bar
        if (connectionState != ConnectionState.CONNECTED) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = when (connectionState) {
                    ConnectionState.CONNECTING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ConnectionState.ERROR -> Color(0xFFFFEBEE)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = when (connectionState) {
                        ConnectionState.CONNECTING -> "Conectando..."
                        ConnectionState.ERROR -> "Erro de conexão"
                        ConnectionState.DISCONNECTED -> "Desconectado"
                        ConnectionState.CONNECTED -> ""
                    },
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = when (connectionState) {
                        ConnectionState.CONNECTING -> MaterialTheme.colorScheme.primary
                        ConnectionState.ERROR -> Color(0xFFD32F2F)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        
        // Attachments row
        if (showAttachments) {
            LazyRow(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AttachmentOption(
                        icon = Icons.Filled.LocationOn,
                        text = "Localização",
                        onClick = {
                            onSendLocation(-23.5505, -46.6333, "São Paulo, SP")
                            showAttachments = false
                        }
                    )
                }
                
                item {
                    AttachmentOption(
                        icon = Icons.Filled.Gif,
                        text = "GIF",
                        onClick = {
                            onSendGif("https://example.com/gif")
                            showAttachments = false
                        }
                    )
                }
            }
        }
        
        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            IconButton(
                onClick = { showAttachments = !showAttachments },
                enabled = connectionState == ConnectionState.CONNECTED
            ) {
                Icon(
                    if (showAttachments) Icons.Filled.Close else Icons.Filled.Add,
                    contentDescription = "Anexos",
                    tint = if (connectionState == ConnectionState.CONNECTED) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    }
                )
            }
            
            OutlinedTextField(
                value = currentMessage,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Digite uma mensagem...") },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                enabled = connectionState == ConnectionState.CONNECTED,
                trailingIcon = if (isTyping) {
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                } else null
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            FloatingActionButton(
                onClick = onSendMessage,
                modifier = Modifier.size(48.dp),
                containerColor = if (connectionState == ConnectionState.CONNECTED && currentMessage.isNotBlank()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "Enviar",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}