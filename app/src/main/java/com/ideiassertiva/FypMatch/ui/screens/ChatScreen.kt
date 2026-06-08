package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ideiassertiva.FypMatch.model.ConnectionDimensions
import com.ideiassertiva.FypMatch.model.ConnectionStatus
import com.ideiassertiva.FypMatch.model.LiveConnection
import com.ideiassertiva.FypMatch.model.Message
import com.ideiassertiva.FypMatch.model.MessageStatus
import com.ideiassertiva.FypMatch.model.MessageType
import com.ideiassertiva.FypMatch.model.User
import com.ideiassertiva.FypMatch.ui.components.ConnectionStatusHeader
import com.ideiassertiva.FypMatch.ui.components.DilemmaBottomSheet
import com.ideiassertiva.FypMatch.ui.components.ErrorState
import com.ideiassertiva.FypMatch.ui.viewmodel.ChatUiState
import com.ideiassertiva.FypMatch.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    currentUserId: String,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showAISuggestions by remember { mutableStateOf(false) }
    var showConnectionRadar by remember { mutableStateOf(false) }
    var selectedMessageForAnalysis by remember { mutableStateOf<String?>(null) }

    val chatData = uiState as? ChatUiState.Success
    val chatError = uiState as? ChatUiState.Error

    LaunchedEffect(conversationId, currentUserId) {
        viewModel.loadConversation(conversationId, currentUserId)
    }

    LaunchedEffect(chatData?.messages?.size) {
        val messages = chatData?.messages ?: return@LaunchedEffect
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.lastIndex)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ChatHeader(
            otherUser = chatData?.otherUser,
            isOnline = chatData?.conversation?.isOtherUserOnline(currentUserId) ?: false,
            lastSeen = viewModel.getLastSeenText(),
            connectionStatus = chatData?.liveConnection?.status,
            onConnectionClick = { showConnectionRadar = true },
            onBackClick = onBackClick
        )

        Box(modifier = Modifier.weight(1f)) {
            when (uiState) {
                is ChatUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ChatUiState.Error -> {
                    ErrorState(
                        icon = Icons.Default.Error,
                        title = "Erro ao carregar",
                        description = chatError?.message.orEmpty(),
                        onRetry = { viewModel.loadConversation(conversationId, currentUserId) }
                    )
                }

                is ChatUiState.Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatData?.messages.orEmpty()) { message ->
                            MessageItem(
                                message = message,
                                isOwnMessage = message.senderId == currentUserId,
                                otherUser = chatData?.otherUser,
                                onReactionClick = { messageId, emoji ->
                                    viewModel.addReaction(messageId, emoji)
                                },
                                onAIAnalysisClick = { messageId ->
                                    selectedMessageForAnalysis = messageId
                                },
                                getStatusIcon = viewModel::getMessageStatusIcon
                            )
                        }

                        if (viewModel.isOtherUserTyping()) {
                            item {
                                TypingIndicator(otherUser = chatData?.otherUser)
                            }
                        }
                    }
                }
            }
        }

        if (showAISuggestions && chatData != null) {
            AISuggestionsCard(
                currentMessage = chatData.currentMessage,
                conversationContext = chatData.messages.takeLast(5),
                onSuggestionSelect = { suggestion ->
                    viewModel.updateMessageText(suggestion)
                    showAISuggestions = false
                },
                onDismiss = { showAISuggestions = false }
            )
        }

        ChatInput(
            currentMessage = chatData?.currentMessage.orEmpty(),
            onMessageChange = viewModel::updateMessageText,
            onSendMessage = viewModel::sendMessage,
            onSendLocation = viewModel::sendLocation,
            onSendGif = viewModel::sendGif,
            onSendMission = viewModel::sendConnectionMission,
            onAISuggestionsClick = { showAISuggestions = !showAISuggestions }
        )
    }

    if (showConnectionRadar) {
        ConnectionRadarSheet(
            connection = chatData?.liveConnection,
            onDismiss = { showConnectionRadar = false }
        )
    }

    selectedMessageForAnalysis?.let { messageId ->
        val message = chatData?.messages?.find { it.id == messageId }
        if (message != null) {
            AIAnalysisModal(
                message = message,
                isOwnMessage = message.senderId == currentUserId,
                conversationContext = chatData.messages,
                onDismiss = { selectedMessageForAnalysis = null }
            )
        }
    }

    if (chatError != null) {
        LaunchedEffect(chatError.message) {
            viewModel.clearError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader(
    otherUser: User?,
    isOnline: Boolean,
    lastSeen: String,
    connectionStatus: ConnectionStatus?,
    onConnectionClick: () -> Unit,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box {
                    AsyncImage(
                        model = otherUser?.profile?.photos?.firstOrNull(),
                        contentDescription = "Foto do usuário",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    if (isOnline) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = otherUser?.profile?.fullName ?: "Usuário",
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isOnline) "Online" else lastSeen,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        if (connectionStatus != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            ConnectionStatusHeader(
                                status = connectionStatus,
                                onClick = onConnectionClick
                            )
                        }
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun MessageItem(
    message: Message,
    isOwnMessage: Boolean,
    otherUser: User?,
    onReactionClick: (String, String) -> Unit,
    onAIAnalysisClick: (String) -> Unit,
    getStatusIcon: (MessageStatus) -> String
) {
    var showReactions by remember { mutableStateOf(false) }
    val isSystemMessage = message.type == MessageType.SYSTEM_INFO

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = when {
            isSystemMessage -> Alignment.CenterHorizontally
            isOwnMessage -> Alignment.End
            else -> Alignment.Start
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = when {
                isSystemMessage -> Arrangement.Center
                isOwnMessage -> Arrangement.End
                else -> Arrangement.Start
            },
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isOwnMessage && !isSystemMessage) {
                AsyncImage(
                    model = otherUser?.profile?.photos?.firstOrNull(),
                    contentDescription = "Foto",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start) {
                val bubbleColor = when {
                    isSystemMessage -> MaterialTheme.colorScheme.secondaryContainer
                    isOwnMessage -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val contentColor = contentColorFor(bubbleColor)

                Surface(
                    modifier = Modifier
                        .clickable(enabled = message.type == MessageType.TEXT) {
                            showReactions = !showReactions
                        }
                        .widthIn(max = 280.dp),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOwnMessage || isSystemMessage) 16.dp else 4.dp,
                        bottomEnd = if (isOwnMessage && !isSystemMessage) 4.dp else 16.dp
                    ),
                    color = bubbleColor
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        MessageContent(
                            message = message,
                            isOwnMessage = isOwnMessage,
                            contentColor = contentColor
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                color = contentColor.copy(alpha = 0.7f)
                            )
                            if (isOwnMessage && !isSystemMessage) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = getStatusIcon(message.status),
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                    color = contentColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                if (message.type == MessageType.TEXT && !isSystemMessage) {
                    TextButton(
                        onClick = { onAIAnalysisClick(message.id) },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Análise de IA",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "IA",
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (message.reactions.isNotEmpty()) {
                    val groupedReactions = message.reactions.groupBy { it.emoji }
                    LazyRow(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(groupedReactions.toList()) { (emoji, reactions) ->
                            ReactionChip(
                                emoji = emoji,
                                count = reactions.size,
                                onClick = { onReactionClick(message.id, emoji) }
                            )
                        }
                    }
                }

                if (showReactions) {
                    LazyRow(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf("❤️", "😂", "😮", "😢", "👍")) { emoji ->
                            Surface(
                                modifier = Modifier
                                    .clickable {
                                        onReactionClick(message.id, emoji)
                                        showReactions = false
                                    }
                                    .size(32.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = emoji, fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                                }
                            }
                        }
                    }
                }
            }

            if (isOwnMessage && !isSystemMessage) {
                Spacer(modifier = Modifier.width(8.dp))
                AsyncImage(
                    model = "https://picsum.photos/400/600?random=current_user",
                    contentDescription = "Minha foto",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun MessageContent(
    message: Message,
    isOwnMessage: Boolean,
    contentColor: Color
) {
    when (message.type) {
        MessageType.LOCATION -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = "Localização",
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = message.content,
                    color = contentColor,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                )
            }
        }

        MessageType.GIF,
        MessageType.IMAGE,
        MessageType.AUDIO,
        MessageType.VIDEO,
        MessageType.STICKER -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = message.getDisplayContent(),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (message.content.isNotBlank()) message.content else message.getDisplayContent(),
                    color = contentColor,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                )
            }
        }

        MessageType.TEXT,
        MessageType.SYSTEM_INFO -> {
            Text(
                text = message.content,
                color = contentColor,
                fontSize = if (isOwnMessage) {
                    MaterialTheme.typography.bodyMedium.fontSize
                } else {
                    MaterialTheme.typography.bodySmall.fontSize
                }
            )
        }
    }
}

@Composable
fun AISuggestionsCard(
    currentMessage: String,
    conversationContext: List<Message>,
    onSuggestionSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val suggestions = generateAISuggestions(currentMessage, conversationContext)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "IA",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Sugestões de IA",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Fechar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            suggestions.forEach { suggestion ->
                OutlinedButton(
                    onClick = { onSuggestionSelect(suggestion.text) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Column {
                        Text(
                            suggestion.text,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            suggestion.reason,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AIAnalysisModal(
    message: Message,
    isOwnMessage: Boolean,
    conversationContext: List<Message>,
    onDismiss: () -> Unit
) {
    val analysis = generateMessageAnalysis(message, isOwnMessage, conversationContext)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "IA",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Análise de IA")
            }
        },
        text = {
            LazyColumn {
                item {
                    Text(
                        "Mensagem:",
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        message.content,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(analysis) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                item.category,
                                fontWeight = FontWeight.Bold,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                item.analysis,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun ReactionChip(
    emoji: String,
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = MaterialTheme.typography.bodySmall.fontSize)
            if (count > 1) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = count.toString(),
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TypingIndicator(otherUser: User?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        AsyncImage(
            model = otherUser?.profile?.photos?.firstOrNull(),
            contentDescription = "Foto",
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.5f + (index * 0.2f)
                                )
                            )
                    )
                    if (index < 2) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInput(
    currentMessage: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSendLocation: (Double, Double, String?) -> Unit,
    onSendGif: (String) -> Unit,
    onSendMission: (String) -> Unit,
    onAISuggestionsClick: () -> Unit
) {
    var showAttachments by remember { mutableStateOf(false) }
    var showDilemmas by remember { mutableStateOf(false) }

    if (showDilemmas) {
        DilemmaBottomSheet(
            onDismiss = { showDilemmas = false },
            onDilemmaSelected = { mission ->
                onSendMission(mission)
            }
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column {
            if (showAttachments) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                            icon = Icons.Filled.Star,
                            text = "Missão",
                            onClick = {
                                showDilemmas = true
                                showAttachments = false
                            }
                        )
                    }

                    item {
                        AttachmentOption(
                            icon = Icons.Filled.Add,
                            text = "GIF",
                            onClick = {
                                onSendGif("https://example.com/gif")
                                showAttachments = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(onClick = { showAttachments = !showAttachments }) {
                    Icon(
                        if (showAttachments) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = "Anexos"
                    )
                }

                OutlinedTextField(
                    value = currentMessage,
                    onValueChange = onMessageChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Digite uma mensagem...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onAISuggestionsClick,
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    )
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Sugestões de IA",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = onSendMessage,
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Enviar",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentOption(
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
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectionRadarSheet(
    connection: LiveConnection?,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Radar da Conexão",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusDescription(connection?.status),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            val dimensions = connection?.dimensions ?: ConnectionDimensions()
            val dimensionRows = listOf(
                "Reciprocidade" to dimensions.reciprocity,
                "Continuidade" to dimensions.continuity,
                "Afinidade" to dimensions.affinity,
                "Leveza" to dimensions.lightness,
                "Profundidade" to dimensions.depth,
                "Iniciativa" to dimensions.initiative
            )

            dimensionRows.forEach { (label, value) ->
                DimensionRow(label = label, value = value)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DimensionRow(label: String, value: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = qualitativeLabel(value),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

private fun statusDescription(status: ConnectionStatus?): String {
    return when (status) {
        ConnectionStatus.ON_FIRE -> "A conversa está com bastante energia e sinais de sintonia."
        ConnectionStatus.ACTIVE -> "A conexão está ganhando ritmo de forma saudável."
        ConnectionStatus.WARMING_UP -> "Vocês estão começando a construir presença e afinidade."
        ConnectionStatus.COOLING_DOWN -> "Há espaço para retomar a conversa com leveza."
        ConnectionStatus.ICE_COLD -> "A conexão ainda tem poucos sinais para interpretar."
        null -> "A conexão começa a aparecer conforme vocês conversam."
    }
}

private fun qualitativeLabel(value: Float): String {
    return when {
        value >= 70f -> "Muito presente"
        value >= 45f -> "Em crescimento"
        value >= 15f -> "Começando"
        else -> "Poucos sinais"
    }
}

data class AISuggestion(
    val text: String,
    val reason: String
)

data class AIAnalysisItem(
    val category: String,
    val analysis: String
)

fun generateAISuggestions(currentMessage: String, context: List<Message>): List<AISuggestion> {
    val lastMessage = context.lastOrNull()
    val suggestions = mutableListOf<AISuggestion>()

    when {
        currentMessage.isEmpty() -> {
            when {
                lastMessage?.content?.contains("como vai", ignoreCase = true) == true -> {
                    suggestions.addAll(
                        listOf(
                            AISuggestion("Tudo bem! E você, como está?", "Resposta calorosa e demonstra interesse"),
                            AISuggestion("Oi! Estou bem, obrigado(a) por perguntar 😊", "Tom amigável com emoji"),
                            AISuggestion("Bem demais! Como foi seu dia?", "Positiva e muda o foco para a pessoa")
                        )
                    )
                }
                lastMessage?.content?.contains("que faz", ignoreCase = true) == true -> {
                    suggestions.addAll(
                        listOf(
                            AISuggestion("Trabalho com [área], adoro o que faço! E você?", "Profissional mas pessoal"),
                            AISuggestion("Sou [profissão], e nas horas vagas gosto de [hobby]. E você, o que curte fazer?", "Completa e demonstra interesse"),
                            AISuggestion("Trabalho na área de [área]. Mas me fala de você!", "Breve e direciona para a pessoa")
                        )
                    )
                }
                else -> {
                    suggestions.addAll(
                        listOf(
                            AISuggestion("Oi! Como está seu dia?", "Cumprimento caloroso e interessado"),
                            AISuggestion("Que bom que deu match! Como você está?", "Reconhece o match e demonstra interesse"),
                            AISuggestion("Olá! Vi que curte [interesse]. Eu também!", "Personalizada baseada no perfil")
                        )
                    )
                }
            }
        }

        currentMessage.length > 100 -> {
            suggestions.add(
                AISuggestion(
                    currentMessage.take(80) + "...",
                    "Versão mais concisa - mensagens longas podem intimidar"
                )
            )
        }

        currentMessage.contains("?") -> {
            suggestions.add(
                AISuggestion(
                    currentMessage.replace("?", " 😊?"),
                    "Adiciona emoji para deixar a pergunta mais amigável"
                )
            )
        }

        !currentMessage.contains(".") && !currentMessage.contains("!") && !currentMessage.contains("?") -> {
            suggestions.addAll(
                listOf(
                    AISuggestion("$currentMessage!", "Tom mais animado"),
                    AISuggestion("$currentMessage 😊", "Adiciona emoji amigável"),
                    AISuggestion("$currentMessage.", "Tom mais formal")
                )
            )
        }
    }

    return suggestions.take(3)
}

fun generateMessageAnalysis(message: Message, isOwnMessage: Boolean, context: List<Message>): List<AIAnalysisItem> {
    val analysis = mutableListOf<AIAnalysisItem>()
    val content = message.content.lowercase()

    val tone = when {
        content.contains("haha") || content.contains("kkk") || content.contains("😂") -> "Bem-humorado"
        content.contains("desculpa") || content.contains("me perdoa") -> "Apologético"
        content.contains("amor") || content.contains("❤️") || content.contains("😍") -> "Romântico"
        content.contains("não") || content.contains("mas") || content.endsWith("...") -> "Hesitante"
        content.contains("!") && !content.contains("?") -> "Entusiasmado"
        content.contains("ok") || content.contains("tá") -> "Neutro"
        else -> "Amigável"
    }

    analysis.add(
        AIAnalysisItem(
            "Tom da mensagem",
            if (isOwnMessage) {
                "Seu tom foi: $tone. ${getToneAdvice(tone, true)}"
            } else {
                "O tom da pessoa foi: $tone. ${getToneAdvice(tone, false)}"
            }
        )
    )

    when {
        message.content.length < 10 -> {
            analysis.add(
                AIAnalysisItem(
                    "Comprimento",
                    if (isOwnMessage) {
                        "Mensagem muito curta. Pode parecer desinteresse. Tente elaborar mais."
                    } else {
                        "Resposta curta pode indicar pressa ou timidez."
                    }
                )
            )
        }
        message.content.length > 200 -> {
            analysis.add(
                AIAnalysisItem(
                    "Comprimento",
                    if (isOwnMessage) {
                        "Mensagem longa. Pode ser intimidante no início. Considere dividir em partes."
                    } else {
                        "Pessoa está muito envolvida na conversa - sinal positivo!"
                    }
                )
            )
        }
    }

    val previousMessages = context.takeLast(3)
    if (previousMessages.size > 1) {
        val responseTime = "rápida"
        analysis.add(
            AIAnalysisItem(
                "Contexto da conversa",
                if (isOwnMessage) {
                    "Você respondeu de forma $responseTime. Isso demonstra ${getResponseTimeAdvice(responseTime)}."
                } else {
                    "A pessoa respondeu de forma $responseTime, indicando ${getResponseTimeAdvice(responseTime)}."
                }
            )
        )
    }

    if (isOwnMessage) {
        analysis.add(
            AIAnalysisItem(
                "Sugestão",
                when {
                    !content.contains("?") -> "Considere fazer uma pergunta para manter a conversa fluindo."
                    content.contains("eu") && !content.contains("você") -> "Tente focar mais na pessoa e menos em si mesmo."
                    else -> "Boa mensagem! Continue assim."
                }
            )
        )
    } else {
        analysis.add(
            AIAnalysisItem(
                "Interpretação",
                when {
                    content.contains("?") -> "A pessoa está interessada em você e quer conhecê-lo melhor."
                    content.contains("trabalho") || content.contains("estudo") -> "Está compartilhando aspectos importantes da vida."
                    content.contains("também") -> "Está buscando pontos em comum - sinal positivo!"
                    else -> "Mensagem neutra, mas o engajamento na conversa é positivo."
                }
            )
        )
    }

    return analysis
}

private fun getToneAdvice(tone: String, isOwnMessage: Boolean): String {
    return when (tone) {
        "Bem-humorado" -> if (isOwnMessage) "Ótimo! Humor quebra o gelo." else "A pessoa está confortável e se divertindo."
        "Romântico" -> if (isOwnMessage) "Cuidado para não ser muito intenso no início." else "Demonstra interesse genuíno."
        "Hesitante" -> if (isOwnMessage) "Tente ser mais direto e confiante." else "Pode estar nervosa - seja acolhedor."
        "Entusiasmado" -> if (isOwnMessage) "Perfeito! Energia positiva é atrativa." else "Está animada com a conversa!"
        else -> "Tom adequado para esta fase da conversa."
    }
}

private fun getResponseTimeAdvice(responseTime: String): String {
    return when (responseTime) {
        "rápida" -> "interesse e disponibilidade"
        "moderada" -> "equilíbrio saudável"
        "lenta" -> "pessoa ocupada ou mais reservada"
        else -> "padrão normal de resposta"
    }
}
