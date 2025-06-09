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
    var selectedMessageForAnalysis by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(conversationId) {
        viewModel.loadConversation(conversationId, currentUserId)
    }
    
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
        // Header melhorado com foto e nome
        ChatHeader(
            otherUser = uiState.otherUser,
            isOnline = uiState.conversation?.isOtherUserOnline(currentUserId) ?: false,
            lastSeen = viewModel.getLastSeenText(),
            onBackClick = onBackClick
        )
        
        // Mensagens
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages) { message ->
                    MessageItem(
                        message = message,
                        isOwnMessage = message.senderId == currentUserId,
                        otherUser = uiState.otherUser,
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
                        TypingIndicator(otherUser = uiState.otherUser)
                    }
                }
            }
        }
        
        // Sugestões de IA
        if (showAISuggestions) {
            AISuggestionsCard(
                currentMessage = uiState.currentMessage,
                conversationContext = uiState.messages.takeLast(5),
                onSuggestionSelect = { suggestion ->
                    viewModel.updateMessageText(suggestion)
                    showAISuggestions = false
                },
                onDismiss = { showAISuggestions = false }
            )
        }
        
        // Input melhorado com botão de IA
        ChatInput(
            currentMessage = uiState.currentMessage,
            onMessageChange = viewModel::updateMessageText,
            onSendMessage = viewModel::sendMessage,
            onSendLocation = { lat, lng, address ->
                viewModel.sendLocation(lat, lng, address)
            },
            onSendGif = viewModel::sendGif,
            onAISuggestionsClick = { showAISuggestions = !showAISuggestions }
        )
    }
    
    // Modal de análise de IA
    selectedMessageForAnalysis?.let { messageId ->
        val message = uiState.messages.find { it.id == messageId }
        if (message != null) {
            AIAnalysisModal(
                message = message,
                isOwnMessage = message.senderId == currentUserId,
                conversationContext = uiState.messages,
                onDismiss = { selectedMessageForAnalysis = null }
            )
        }
    }
    
    // Snackbar para erros
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Em um app real, mostraria Snackbar
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
                                .background(Color.Green)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = otherUser?.profile?.fullName ?: "Usuário",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isOnline) "Online" else lastSeen,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
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
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isOwnMessage && message.type != MessageType.SYSTEM_INFO) {
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
            
            Column(
                horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
            ) {
                // Bolha da mensagem
                Surface(
                    modifier = Modifier
                        .clickable { 
                            if (message.type == MessageType.TEXT) {
                                showReactions = !showReactions 
                            }
                        }
                        .widthIn(max = 280.dp),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                        bottomEnd = if (isOwnMessage) 4.dp else 16.dp
                    ),
                    color = if (isOwnMessage) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Conteúdo da mensagem baseado no tipo
                        when (message.type) {
                            MessageType.TEXT -> {
                                Text(
                                    text = message.content,
                                    color = if (isOwnMessage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                            MessageType.LOCATION -> {
                                Column {
                                    Icon(
                                        Icons.Filled.LocationOn,
                                        contentDescription = "Localização",
                                        tint = if (isOwnMessage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = message.content,
                                        color = if (isOwnMessage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            MessageType.GIF -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = "GIF",
                                        tint = if (isOwnMessage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "GIF",
                                        color = if (isOwnMessage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            else -> {
                                Text(
                                    text = message.content,
                                    color = if (isOwnMessage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        
                        // Timestamp e status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                                fontSize = 10.sp,
                                color = if (isOwnMessage) Color.White.copy(alpha = 0.7f) 
                                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            if (isOwnMessage) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = getStatusIcon(message.status),
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                
                // Botão de análise de IA
                if (message.type == MessageType.TEXT) {
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
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Reações (específicas para esta mensagem)
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
                
                // Painel de reações (específico para esta mensagem)
                if (showReactions) {
                    LazyRow(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf("❤️", "😂", "😮", "😢", "😡", "👍")) { emoji ->
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
                                    Text(text = emoji, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
            
            if (isOwnMessage && message.type != MessageType.SYSTEM_INFO) {
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
                            fontSize = 14.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            suggestion.reason,
                            fontSize = 12.sp,
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
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        message.content,
                        fontSize = 14.sp,
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
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                item.analysis,
                                fontSize = 14.sp
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
            Text(text = emoji, fontSize = 12.sp)
            if (count > 1) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = count.toString(),
                    fontSize = 10.sp,
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
    onAISuggestionsClick: () -> Unit
) {
    var showAttachments by remember { mutableStateOf(false) }
    
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
                    .padding(horizontal = 16.dp, vertical = 24.dp) // Padding maior para evitar sobreposição
                    .padding(bottom = 32.dp), // Padding extra ainda maior
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
                
                // Botão de IA
                IconButton(
                    onClick = onAISuggestionsClick,
                    modifier = Modifier
                        .background(
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
                        tint = Color.White
                    )
                }
            }
            
            // Spacer final para garantir espaço extra
            Spacer(modifier = Modifier.height(16.dp))
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
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Modelos de dados para IA
data class AISuggestion(
    val text: String,
    val reason: String
)

data class AIAnalysisItem(
    val category: String,
    val analysis: String
)

// Funções de IA para análise e sugestões
fun generateAISuggestions(currentMessage: String, context: List<Message>): List<AISuggestion> {
    val lastMessage = context.lastOrNull()
    val suggestions = mutableListOf<AISuggestion>()
    
    when {
        currentMessage.isEmpty() -> {
            when {
                lastMessage?.content?.contains("como vai", ignoreCase = true) == true -> {
                    suggestions.addAll(listOf(
                        AISuggestion("Tudo bem! E você, como está?", "Resposta calorosa e demonstra interesse"),
                        AISuggestion("Oi! Estou bem, obrigado(a) por perguntar 😊", "Tom amigável com emoji"),
                        AISuggestion("Bem demais! Como foi seu dia?", "Positiva e muda o foco para a pessoa")
                    ))
                }
                lastMessage?.content?.contains("que faz", ignoreCase = true) == true -> {
                    suggestions.addAll(listOf(
                        AISuggestion("Trabalho com [área], adoro o que faço! E você?", "Profissional mas pessoal"),
                        AISuggestion("Sou [profissão], e nas horas vagas gosto de [hobby]. E você, o que curte fazer?", "Completa e demonstra interesse"),
                        AISuggestion("Trabalho na área de [área]. Mas me fala de você!", "Breve e direciona para a pessoa")
                    ))
                }
                else -> {
                    suggestions.addAll(listOf(
                        AISuggestion("Oi! Como está seu dia?", "Cumprimento caloroso e interessado"),
                        AISuggestion("Que bom que deu match! Como você está?", "Reconhece o match e demonstra interesse"),
                        AISuggestion("Olá! Vi que curte [interesse]. Eu também!", "Personalizada baseada no perfil")
                    ))
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
            suggestions.addAll(listOf(
                AISuggestion("$currentMessage!", "Tom mais animado"),
                AISuggestion("$currentMessage 😊", "Adiciona emoji amigável"),
                AISuggestion("$currentMessage.", "Tom mais formal")
            ))
        }
    }
    
    return suggestions.take(3)
}

fun generateMessageAnalysis(message: Message, isOwnMessage: Boolean, context: List<Message>): List<AIAnalysisItem> {
    val analysis = mutableListOf<AIAnalysisItem>()
    val content = message.content.lowercase()
    
    // Análise de tom
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
            if (isOwnMessage) 
                "Seu tom foi: $tone. ${getToneAdvice(tone, true)}"
            else 
                "O tom da pessoa foi: $tone. ${getToneAdvice(tone, false)}"
        )
    )
    
    // Análise de comprimento
    when {
        message.content.length < 10 -> {
            analysis.add(
                AIAnalysisItem(
                    "Comprimento",
                    if (isOwnMessage)
                        "Mensagem muito curta. Pode parecer desinteresse. Tente elaborar mais."
                    else
                        "Resposta curta pode indicar pressa ou timidez."
                )
            )
        }
        message.content.length > 200 -> {
            analysis.add(
                AIAnalysisItem(
                    "Comprimento",
                    if (isOwnMessage)
                        "Mensagem longa. Pode ser intimidante no início. Considere dividir em partes."
                    else
                        "Pessoa está muito envolvida na conversa - sinal positivo!"
                )
            )
        }
    }
    
    // Análise contextual
    val previousMessages = context.takeLast(3)
    if (previousMessages.size > 1) {
        val responseTime = "rápida" // Simulado
        analysis.add(
            AIAnalysisItem(
                "Contexto da conversa",
                if (isOwnMessage)
                    "Você respondeu de forma $responseTime. Isso demonstra ${getResponseTimeAdvice(responseTime)}."
                else
                    "A pessoa respondeu de forma $responseTime, indicando ${getResponseTimeAdvice(responseTime)}."
            )
        )
    }
    
    // Sugestões de melhoria
    if (isOwnMessage) {
        analysis.add(
            AIAnalysisItem(
                "Sugestão",
                when {
                    !content.contains("?") -> "Considere fazer uma pergunta para manter a conversa fluindo."
                    content.contains("eu") > content.contains("você") -> "Tente focar mais na pessoa e menos em si mesmo."
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
