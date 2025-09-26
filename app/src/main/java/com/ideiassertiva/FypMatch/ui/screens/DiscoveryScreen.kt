package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme
import com.ideiassertiva.FypMatch.ui.viewmodel.DiscoveryViewModel
import com.ideiassertiva.FypMatch.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToPhase3Demo: () -> Unit = {},
    viewModel: DiscoveryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentCard by viewModel.currentCard.collectAsStateWithLifecycle()
    
    // Mostrar modal de match se houve match
    if (uiState.showMatchModal && uiState.lastMatch != null) {
        MatchModal(
            match = uiState.lastMatch!!,
            onDismiss = { viewModel.dismissMatchModal() },
            onSendMessage = { 
                uiState.conversationId?.let { conversationId ->
                    onNavigateToChat(conversationId)
                }
                viewModel.dismissMatchModal()
            }
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
            onProfileClick = onNavigateToProfile,
            onPhase3DemoClick = onNavigateToPhase3Demo
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
        
        // Spacer maior para não ficar embaixo da navegação do Android
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DiscoveryTopBar(
    onSettingsClick: () -> Unit,
    onMatchesClick: () -> Unit,
    onAICounselorClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onPhase3DemoClick: () -> Unit = {}
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
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "FypMatch Logo",
                modifier = Modifier
                    .height(40.dp)
                    .width(120.dp),
                contentScale = ContentScale.Fit
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Botão do conselheiro VIP
                OutlinedButton(
                    onClick = onAICounselorClick,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "IA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Botão Phase 3 Demo - NOVO!
                OutlinedButton(
                    onClick = onPhase3DemoClick,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Chat",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SwipeCard(
    card: DiscoveryCard,
    onSwipe: (SwipeType) -> Unit,
    onCardClick: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    
    // Estados das animações melhoradas
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = if (isDragging) {
            spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
        } else {
            spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
        },
        label = "offsetX"
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = if (isDragging) {
            spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
        } else {
            spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
        },
        label = "offsetY"
    )
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = if (isDragging) {
            spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
        } else {
            spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
        },
        label = "rotation"
    )
    
    val threshold = 150f // Aumente o threshold para swipes mais precisos
    val superLikeThreshold = 120f
    
    // Estado do pager para fotos - seguindo padrão do Tinder
    val photos = card.user.profile.photos.takeIf { it.isNotEmpty() } 
        ?: listOf("https://picsum.photos/400/600?random=${card.user.id}")
    val pagerState = rememberPagerState(pageCount = { photos.size })
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.65f) // Mais próximo do Tinder
            .graphicsLayer {
                translationX = animatedOffsetX
                translationY = animatedOffsetY
                rotationZ = animatedRotation
                // Efeito de escala mais sutil durante o swipe
                val scale = 1f - (abs(offsetX) / 2000f).coerceAtMost(0.05f)
                scaleX = scale
                scaleY = scale
                alpha = (1f - (abs(offsetX) / 1000f)).coerceIn(0.7f, 1f)
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        isDragging = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDragEnd = {
                        isDragging = false
                        when {
                            abs(offsetX) > threshold -> {
                                val swipeType = if (offsetX > 0) SwipeType.LIKE else SwipeType.PASS
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                
                                // Animação de saída mais dramática
                                coroutineScope.launch {
                                    offsetX = if (offsetX > 0) 1000f else -1000f
                                    rotation = if (offsetX > 0) 30f else -30f
                                    delay(200)
                                    onSwipe(swipeType)
                                    offsetX = 0f
                                    offsetY = 0f
                                    rotation = 0f
                                }
                                return@detectDragGestures
                            }
                            offsetY < -superLikeThreshold -> {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    offsetY = -800f
                                    delay(200)
                                    onSwipe(SwipeType.SUPER_LIKE)
                                    offsetX = 0f
                                    offsetY = 0f
                                    rotation = 0f
                                }
                                return@detectDragGestures
                            }
                            else -> {
                                // Reset suave
                                offsetX = 0f
                                offsetY = 0f
                                rotation = 0f
                            }
                        }
                    }
                ) { _, dragAmount ->
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                    rotation = (offsetX / 8f).coerceIn(-25f, 25f) // Rotação mais suave
                    
                    // Feedback háptico nos limiares
                    if (abs(offsetX) > threshold * 0.7f && abs(offsetX) < threshold * 0.8f) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
            },
        shape = RoundedCornerShape(20.dp), // Cantos mais arredondados como o Tinder
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp) // Mais elevação
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Sistema de fotos inspirado no Tinder
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { photos[it] },
                userScrollEnabled = false // Controlado pelos toques laterais
            ) { page ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photos[page])
                        .crossfade(300)
                        .build(),
                    contentDescription = "Foto ${page + 1} de ${card.user.profile.fullName}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val screenWidth = size.width
                                coroutineScope.launch {
                                    if (offset.x < screenWidth / 2) {
                                        // Toque na metade esquerda - foto anterior
                                        if (pagerState.currentPage > 0) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    } else {
                                        // Toque na metade direita - próxima foto
                                        if (pagerState.currentPage < photos.size - 1) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    }
                                }
                            }
                        }
                )
            }
            
            // Indicadores de foto no topo (como o Tinder)
            if (photos.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(photos.size) { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(
                                    if (index <= pagerState.currentPage) Color.White 
                                    else Color.White.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }
            
            // Indicadores de swipe melhorados
            SwipeIndicators(
                offsetX = offsetX, 
                offsetY = offsetY, 
                threshold = threshold,
                superLikeThreshold = superLikeThreshold
            )
            
            // Gradient overlay mais sutil
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 200f
                        )
                    )
            )
            
            // Botão de informações mais discreto
            IconButton(
                onClick = onCardClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.4f),
                        CircleShape
                    )
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Ver mais detalhes",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            // Informações do usuário melhoradas
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${card.user.profile.fullName}, ${card.user.profile.age}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    if (card.isVerified) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Verificado",
                            tint = Color(0xFF4FC3F7),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Text(
                    text = "${card.distance}km de distância",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
                
                if (card.user.profile.profession.isNotBlank()) {
                    Text(
                        text = card.user.profile.profession,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                if (card.user.profile.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = card.user.profile.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2
                    )
                }
                
                // Score de compatibilidade mais discreto
                if (card.compatibilityScore > 0.6f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${(card.compatibilityScore * 100).toInt()}% compatível",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE91E63)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeIndicators(
    offsetX: Float,
    offsetY: Float,
    threshold: Float,
    superLikeThreshold: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Indicador LIKE (direita) - melhorado
        if (offsetX > threshold * 0.4f) {
            val alpha = ((offsetX - threshold * 0.4f) / (threshold * 0.6f)).coerceIn(0f, 1f)
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .rotate(15f)
                    .graphicsLayer { this.alpha = alpha },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                border = BorderStroke(4.dp, Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "CURTIR",
                    modifier = Modifier.padding(24.dp, 12.dp),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
        
        // Indicador PASS (esquerda) - melhorado
        if (offsetX < -threshold * 0.4f) {
            val alpha = ((-offsetX - threshold * 0.4f) / (threshold * 0.6f)).coerceIn(0f, 1f)
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .rotate(-15f)
                    .graphicsLayer { this.alpha = alpha },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336)),
                border = BorderStroke(4.dp, Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "PASSAR",
                    modifier = Modifier.padding(24.dp, 12.dp),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
        
        // Indicador SUPER LIKE (cima) - melhorado
        if (offsetY < -superLikeThreshold * 0.4f) {
            val alpha = ((-offsetY - superLikeThreshold * 0.4f) / (superLikeThreshold * 0.6f)).coerceIn(0f, 1f)
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer { this.alpha = alpha },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3)),
                border = BorderStroke(4.dp, Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp, 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SUPER CURTIR",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall
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
    val haptic = LocalHapticFeedback.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botão Rewind (ainda não implementado, mas deixando espaço para futuro)
        FloatingActionButton(
            onClick = { /* TODO: Implementar rewind */ },
            containerColor = Color(0xFFFFC107).copy(alpha = 0.3f),
            modifier = Modifier.size(42.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Voltar",
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Botão Passar - melhorado seguindo padrão Tinder
        FloatingActionButton(
            onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onPassClick() 
            },
            containerColor = Color.White,
            modifier = Modifier.size(54.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Passar",
                tint = Color(0xFFF44336),
                modifier = Modifier.size(28.dp)
            )
        }
        
        // Botão Super Like - melhorado
        FloatingActionButton(
            onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSuperLikeClick() 
            },
            containerColor = Color.White,
            modifier = Modifier.size(44.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Super Curtir",
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(22.dp)
            )
        }
        
        // Botão Curtir - melhorado seguindo padrão Tinder
        FloatingActionButton(
            onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLikeClick() 
            },
            containerColor = Color.White,
            modifier = Modifier.size(54.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Curtir",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(28.dp)
            )
        }
        
        // Botão Boost (ainda não implementado, mas deixando espaço para futuro)
        FloatingActionButton(
            onClick = { /* TODO: Implementar boost */ },
            containerColor = Color(0xFF9C27B0).copy(alpha = 0.3f),
            modifier = Modifier.size(42.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Boost",
                tint = Color(0xFF9C27B0),
                modifier = Modifier.size(20.dp)
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
    FypMatchTheme {
        DiscoveryScreen()
    }
} 
