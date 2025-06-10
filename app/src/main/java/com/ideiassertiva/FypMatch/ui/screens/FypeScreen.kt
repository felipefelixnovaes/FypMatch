package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.PlayArrow

import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ideiassertiva.FypMatch.ui.viewmodel.CreditsViewModel
import com.ideiassertiva.FypMatch.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FypeScreen(
    onBackClick: () -> Unit,
    creditsViewModel: CreditsViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as androidx.activity.ComponentActivity
    
    // Estados dos créditos
    val credits by creditsViewModel.credits.collectAsStateWithLifecycle()
    val isAdLoaded by creditsViewModel.isAdLoaded.collectAsStateWithLifecycle()
    val isLoading by creditsViewModel.isLoading.collectAsStateWithLifecycle()
    val canChatWithFype by creditsViewModel.canChatWithFype.collectAsStateWithLifecycle()
    val showRewardDialog by creditsViewModel.showRewardDialog.collectAsStateWithLifecycle()
    val message by creditsViewModel.message.collectAsStateWithLifecycle()
    
    // Estados do chat
    val messages by chatViewModel.messages.collectAsStateWithLifecycle()
    val isTyping by chatViewModel.isTyping.collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE91E63),
                        Color(0xFF9C27B0),
                        Color(0xFF673AB7)
                    )
                )
            )
    ) {
        // 🎯 Header com créditos
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Face,
                        contentDescription = "Fype",
                        tint = Color.White
                    )
                    Text(
                        "Fype - Sua Conselheira",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
            },
            actions = {
                // 💰 Contador de créditos
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Créditos",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = credits.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
        
        // 💬 Área do chat
        if (canChatWithFype) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true
            ) {
                if (isTyping) {
                    item {
                        TypingIndicator()
                    }
                }
                
                items(messages.reversed()) { message ->
                    MessageBubble(
                        message = message.content,
                        isUser = message.isUser,
                        timestamp = message.timestamp
                    )
                }
                
                // Mensagem de boas-vindas da Fype
                if (messages.isEmpty()) {
                    item {
                        WelcomeMessage()
                    }
                }
            }
            
            // 📝 Campo de entrada de mensagem
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Conte para a Fype...") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE91E63),
                            cursorColor = Color(0xFFE91E63)
                        )
                    )
                    
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                if (creditsViewModel.sendMessageToFype(messageText)) {
                                    chatViewModel.sendMessage(messageText)
                                    messageText = ""
                                }
                            }
                        },
                        enabled = messageText.isNotBlank() && credits > 0
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = if (credits > 0) Color(0xFFE91E63) else Color.Gray
                        )
                    }
                }
                
                // Info sobre custo
                if (credits > 0) {
                    Text(
                        text = "💰 Cada mensagem custa 1 crédito",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // 🎥 Tela para ganhar créditos
            NeedCreditsScreen(
                onWatchAd = { creditsViewModel.watchAdForCredits(activity) },
                isAdLoaded = isAdLoaded,
                isLoading = isLoading
            )
        }
        
        // 🎉 Dialog de recompensa
        if (showRewardDialog) {
            AlertDialog(
                onDismissRequest = { creditsViewModel.closeRewardDialog() },
                title = {
                    Text(
                        "🎉 Parabéns!",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        "Você ganhou 3 créditos para conversar com a Fype!\n\nAgora você pode fazer perguntas sobre relacionamentos e receber conselhos personalizados.",
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { creditsViewModel.closeRewardDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE91E63)
                        )
                    ) {
                        Text("Começar a conversar!")
                    }
                }
            )
        }
        
        // 📢 Mensagens do sistema
        if (message.isNotBlank()) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { creditsViewModel.clearMessage() }) {
                        Text("OK")
                    }
                }
            ) {
                Text(message)
            }
        }
    }
}

@Composable
private fun NeedCreditsScreen(
    onWatchAd: () -> Unit,
    isAdLoaded: Boolean,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Avatar da Fype
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFF9800)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Face,
                contentDescription = "Fype",
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Olá! Eu sou a Fype 💕",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Sua conselheira pessoal de relacionamentos! Para conversar comigo, você precisa de créditos.",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Vídeo",
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Assista a um vídeo e ganhe",
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.7f)
                )
                
                Text(
                    text = "3 CRÉDITOS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE91E63)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = onWatchAd,
                    enabled = isAdLoaded && !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Carregando...")
                    } else if (isAdLoaded) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Assistir Vídeo", fontSize = 16.sp)
                    } else {
                        Text("Carregando anúncio...", fontSize = 16.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Cada crédito permite enviar uma mensagem para a Fype",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WelcomeMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Face,
                    contentDescription = "Fype",
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Fype",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE91E63)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Olá! 💕 Eu sou a Fype, sua conselheira pessoal de relacionamentos. Estou aqui para ajudar você com conselhos sobre amor, encontros e relacionamentos. Pode me contar o que está acontecendo?",
                color = Color.Black.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: String,
    isUser: Boolean,
    timestamp: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    Color(0xFFE91E63)
                } else {
                    Color.White.copy(alpha = 0.95f)
                }
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (!isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Face,
                            contentDescription = "Fype",
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Fype",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE91E63)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Text(
                    text = message,
                    color = if (isUser) Color.White else Color.Black.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = timestamp,
                    fontSize = 10.sp,
                    color = if (isUser) {
                        Color.White.copy(alpha = 0.7f)
                    } else {
                        Color.Gray
                    },
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Face,
                    contentDescription = "Fype",
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Fype está digitando...",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                
                // Animação de digitação
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE91E63).copy(alpha = 0.6f))
                    )
                }
            }
        }
    }
} 