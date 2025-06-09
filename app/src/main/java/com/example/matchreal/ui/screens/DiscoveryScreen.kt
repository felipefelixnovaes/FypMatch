package com.example.matchreal.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.math.abs
import kotlin.math.sign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    modifier: Modifier = Modifier,
    onNavigateToMatches: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    onNavigateToAICounselor: (String) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToUserDetails: (String) -> Unit = {},
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
        // Header melhorado
        DiscoveryTopBar(
            onSettingsClick = { /* TODO: Implementar settings */ },
            onMatchesClick = onNavigateToMatches,
            onAICounselorClick = { onNavigateToAICounselor("current_user_id") },
            onProfileClick = onNavigateToProfile
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
                        onSwipe = { swipeType -> viewModel.performSwipe(swipeType) },
                        onCardClick = { onNavigateToUserDetails(currentCard!!.user.id) }
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
    onAICounselorClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botão de perfil do usuário
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Meu Perfil",
                tint = MaterialTheme.colorScheme.primary
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
            OutlinedButton(
                onClick = onAICounselorClick,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Conselheiro",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Botão de matches
        Box {
            IconButton(
                onClick = onMatchesClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Matches",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Badge de notificação (exemplo)
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 2.dp),
                containerColor = Color.Red
            ) {
                Text(
                    text = "3",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun SwipeCard(
    card: DiscoveryCard,
    onSwipe: (SwipeType) -> Unit,
    onCardClick: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = 0.7f)
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(dampingRatio = 0.7f)
    )
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = spring(dampingRatio = 0.7f)
    )
    
    val screenWidth = 350.dp // Aproximadamente
    val threshold = with(androidx.compose.ui.platform.LocalDensity.current) { 100.dp.toPx() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .graphicsLayer {
                translationX = animatedOffsetX
                translationY = animatedOffsetY
                rotationZ = animatedRotation
                alpha = if (abs(offsetX) > threshold) 0.8f else 1f
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        when {
                            abs(offsetX) > threshold -> {
                                val swipeType = if (offsetX > 0) SwipeType.LIKE else SwipeType.PASS
                                onSwipe(swipeType)
                                offsetX = 0f
                                offsetY = 0f
                                rotation = 0f
                            }
                            offsetY < -threshold -> {
                                onSwipe(SwipeType.SUPER_LIKE)
                                offsetX = 0f
                                offsetY = 0f
                                rotation = 0f
                            }
                            else -> {
                                offsetX = 0f
                                offsetY = 0f
                                rotation = 0f
                            }
                        }
                    }
                ) { _, dragAmount ->
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                    rotation = (offsetX / 10f).coerceIn(-30f, 30f)
                }
            }
            .clickable { onCardClick() },
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
            
            // Indicadores de swipe
            SwipeIndicators(offsetX = offsetX, offsetY = offsetY, threshold = threshold)
            
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
            
            // Botão de mais informações
            IconButton(
                onClick = onCardClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Ver mais detalhes",
                    tint = Color.White
                )
            }
            
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
                    text = "${card.distance}km de distância • ${card.user.profile.profession}",
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
                        text = "Interesses em comum: ${card.commonInterests.take(3).joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                // Score de compatibilidade
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        val isFilled = index < (card.compatibilityScore * 5).toInt()
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isFilled) Color.Yellow else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(card.compatibilityScore * 100).toInt()}% compatível",
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
private fun SwipeIndicators(
    offsetX: Float,
    offsetY: Float,
    threshold: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Indicador LIKE (direita)
        if (offsetX > threshold * 0.3f) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(32.dp)
                    .rotate(15f),
                colors = CardDefaults.cardColors(containerColor = Color.Green),
                border = BorderStroke(3.dp, Color.White)
            ) {
                Text(
                    text = "CURTIR",
                    modifier = Modifier.padding(16.dp, 8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        
        // Indicador PASS (esquerda)
        if (offsetX < -threshold * 0.3f) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(32.dp)
                    .rotate(-15f),
                colors = CardDefaults.cardColors(containerColor = Color.Red),
                border = BorderStroke(3.dp, Color.White)
            ) {
                Text(
                    text = "PASSAR",
                    modifier = Modifier.padding(16.dp, 8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        
        // Indicador SUPER LIKE (cima)
        if (offsetY < -threshold * 0.5f) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 50.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4FC3F7)),
                border = BorderStroke(3.dp, Color.White)
            ) {
                Text(
                    text = "SUPER CURTIR",
                    modifier = Modifier.padding(16.dp, 8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
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