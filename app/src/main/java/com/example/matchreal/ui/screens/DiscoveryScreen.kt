package com.example.matchreal.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.matchreal.model.*
import com.example.matchreal.ui.theme.MatchRealTheme
import com.example.matchreal.ui.viewmodel.DiscoveryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    modifier: Modifier = Modifier,
    onNavigateToMatches: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    onNavigateToAICounselor: (String) -> Unit = {},
    viewModel: DiscoveryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentCard by viewModel.currentCard.collectAsStateWithLifecycle()
    
    // Mostrar modal de match se houve match
    if (uiState.showMatchModal && uiState.lastMatch != null) {
        MatchModal(
            match = uiState.lastMatch!!,
            onDismiss = { viewModel.dismissMatchModal() },
            onSendMessage = { onNavigateToMatches() }
        )
    }
    
    // Mostrar modal de limite atingido
    if (uiState.showLimitModal) {
        LimitReachedModal(
            limitType = uiState.limitType,
            onDismiss = { viewModel.dismissLimitModal() },
            onUpgrade = onNavigateToPremium
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header com botões
        DiscoveryTopBar(
            onSettingsClick = { /* TODO: Implementar settings */ },
            onMatchesClick = onNavigateToMatches,
            onAICounselorClick = { onNavigateToAICounselor("current_user_id") }
        )
        
        // Área dos cards
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                currentCard != null -> {
                    SwipeCard(
                        card = currentCard!!,
                        onSwipe = { swipeType -> viewModel.performSwipe(swipeType) }
                    )
                }
                
                else -> {
                    NoMoreCardsView(
                        onRefresh = { viewModel.refreshCards() }
                    )
                }
            }
        }
        
        // Botões de ação
        SwipeActionButtons(
            onPassClick = { viewModel.performSwipe(SwipeType.PASS) },
            onSuperLikeClick = { viewModel.performSwipe(SwipeType.SUPER_LIKE) },
            onLikeClick = { viewModel.performSwipe(SwipeType.LIKE) },
            enabled = !uiState.isLoading && currentCard != null
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DiscoveryTopBar(
    onSettingsClick: () -> Unit,
    onMatchesClick: () -> Unit,
    onAICounselorClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configurações",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Título e botão do conselheiro
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "💕 MatchReal",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Botão do conselheiro VIP
            TextButton(onClick = onAICounselorClick) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Conselheiro VIP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        IconButton(onClick = onMatchesClick) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Matches",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SwipeCard(
    card: DiscoveryCard,
    onSwipe: (SwipeType) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Foto principal
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(card.user.profile.photos.firstOrNull() ?: "https://picsum.photos/400/600?random=1")
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto de ${card.user.profile.fullName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 400f
                        )
                    )
            )
            
            // Informações do usuário
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${card.user.profile.fullName}, ${card.user.profile.age}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    if (card.isVerified) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Verificado",
                            tint = Color.Yellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Text(
                    text = "${card.distance}km de distância",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                if (card.user.profile.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = card.user.profile.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2
                    )
                }
                
                if (card.commonInterests.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Interesses em comum: ${card.commonInterests.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                // Score de compatibilidade
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Compatibilidade: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${(card.compatibilityScore * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeActionButtons(
    onPassClick: () -> Unit,
    onSuperLikeClick: () -> Unit,
    onLikeClick: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FloatingActionButton(
            onClick = onPassClick,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(56.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Passar",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
        
        FloatingActionButton(
            onClick = onSuperLikeClick,
            containerColor = Color(0xFF4FC3F7),
            modifier = Modifier.size(48.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Super Curtir",
                tint = Color.White
            )
        }
        
        FloatingActionButton(
            onClick = onLikeClick,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Curtir",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun NoMoreCardsView(
    onRefresh: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Não há mais pessoas por aqui!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Volte mais tarde para ver novos perfis",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRefresh,
            modifier = Modifier.height(48.dp)
        ) {
            Text("Atualizar")
        }
    }
}

@Composable
private fun MatchModal(
    match: Match,
    onDismiss: () -> Unit,
    onSendMessage: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🎉 É um Match!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Vocês dois se curtiram! Que tal começar uma conversa?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSendMessage,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar Mensagem")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Talvez depois")
            }
        }
    )
}

@Composable
private fun LimitReachedModal(
    limitType: String,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Limite atingido",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = when (limitType) {
                    "likes" -> "Você atingiu seu limite diário de curtidas! Upgrade para Premium para curtidas ilimitadas."
                    "super_likes" -> "Você atingiu seu limite diário de super curtidas! Upgrade para mais super curtidas."
                    else -> "Limite atingido. Faça upgrade para continuar."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onUpgrade) {
                Text("Upgrade Premium")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Talvez depois")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DiscoveryScreenPreview() {
    MatchRealTheme {
        DiscoveryScreen()
    }
} 