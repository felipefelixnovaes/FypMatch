package com.ideiassertiva.FypMatch.ui.screens

import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.ui.platform.LocalContext
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
import com.ideiassertiva.FypMatch.ui.components.SkeletonLoading
import com.ideiassertiva.FypMatch.ui.theme.FypColors
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme
import com.ideiassertiva.FypMatch.ui.viewmodel.DiscoveryViewModel
import com.ideiassertiva.FypMatch.ui.viewmodel.DiscoveryUiState
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
    onNavigateToPhase4AI: (String) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToUserDetails: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToPhase3Demo: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: DiscoveryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentCard by viewModel.currentCard.collectAsStateWithLifecycle()

    // Modal de match
    val matchModal = uiState as? DiscoveryUiState.MatchModal
    if (matchModal != null) {
        MatchModal(
            match = matchModal.match,
            onDismiss = { viewModel.dismissMatchModal() },
            onSendMessage = {
                onNavigateToChat(matchModal.conversationId)
                viewModel.dismissMatchModal()
            }
        )
    }

    // Modal de limite
    val limitModal = uiState as? DiscoveryUiState.LimitModal
    if (limitModal != null) {
        LimitReachedModal(
            limitType = limitModal.limitType,
            onDismiss = { viewModel.dismissLimitModal() },
            onUpgrade = onNavigateToPremium
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DiscoveryTopBar(
            onSettingsClick = onNavigateToSettings,
            onMatchesClick = onNavigateToMatches,
            onAICounselorClick = { onNavigateToAICounselor("current_user_id") },
            onPhase4AIClick = { onNavigateToPhase4AI("current_user_id") },
            onProfileClick = onNavigateToProfile,
            onPhase3DemoClick = onNavigateToPhase3Demo
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is DiscoveryUiState.Loading -> {
                    SkeletonLoading(count = 3, itemHeight = 200)
                }

                is DiscoveryUiState.Content -> {
                    if (currentCard != null) {
                        SwipeCard(
                            card = currentCard!!,
                            onSwipe = { swipeType -> viewModel.performSwipe(swipeType) },
                            onCardClick = { onNavigateToUserDetails(currentCard!!.user.id) }
                        )
                    } else {
                        NoMoreCardsView(onRefresh = { viewModel.refreshCards() })
                    }
                }

                is DiscoveryUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (uiState as DiscoveryUiState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Tentar novamente")
                        }
                    }
                }

                is DiscoveryUiState.MatchModal,
                is DiscoveryUiState.LimitModal -> { /* modals são renderizados acima */ }
            }
        }

        SwipeActionButtons(
            onPassClick = { viewModel.performSwipe(SwipeType.PASS) },
            onSuperLikeClick = { viewModel.performSwipe(SwipeType.SUPER_LIKE) },
            onLikeClick = { viewModel.performSwipe(SwipeType.LIKE) },
            enabled = uiState !is DiscoveryUiState.Loading && currentCard != null,
            onRewindClick = { viewModel.rewindLastSwipe() },
            onBoostClick = { viewModel.activateBoost() }
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
    onPhase4AIClick: () -> Unit = {},
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
        // Botões de perfil e configurações
        Row(verticalAlignment = Alignment.CenterVertically) {
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
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu e Configurações",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
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
            
            // Único acesso destacado: Conselheiro IA (feature premium)
            OutlinedButton(
                onClick = onAICounselorClick,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Conselheiro IA",
                    style = MaterialTheme.typography.labelMedium,
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
    val context = LocalContext.current

    // Respeita preferência de reduced motion do sistema
    val reducedMotion = remember {
        try {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        } catch (_: Exception) { false }
    }

    // Estados das animações melhoradas — instantâneas se reducedMotion
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = when {
            reducedMotion -> tween(durationMillis = 0)
            isDragging -> spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
            else -> spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
        },
        label = "offsetX"
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = when {
            reducedMotion -> tween(durationMillis = 0)
            isDragging -> spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
            else -> spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
        },
        label = "offsetY"
    )
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = when {
            reducedMotion -> tween(durationMillis = 0)
            isDragging -> spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
            else -> spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
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
                            tint = FypColors.SuperLike,
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

                // Badges de autoconhecimento e perfil
                ProfileBadgesRow(profile = card.user.profile)

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
                            tint = FypColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${(card.compatibilityScore * 100).toInt()}% compatível",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = FypColors.Primary
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
                colors = CardDefaults.cardColors(containerColor = FypColors.Like),
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
                colors = CardDefaults.cardColors(containerColor = FypColors.Pass),
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
                colors = CardDefaults.cardColors(containerColor = FypColors.SuperLike),
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

/** Linha de badges no card: eneagrama, arquétipo, signo, intenção, linguagem do amor.
 *  Usa FlowRow (quebra linha) — NUNCA scroll, para não conflitar com o gesto de swipe. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileBadgesRow(profile: com.ideiassertiva.FypMatch.model.UserProfile) {
    val badges = buildList {
        if (profile.enneagramType.isNotBlank()) {
            add(Icons.Default.Psychology to profile.enneagramType.substringBefore(" —").trim())
        }
        if (profile.personalityArchetype.isNotBlank()) {
            add(Icons.Default.AutoAwesome to profile.personalityArchetype)
        }
        if (profile.loveLanguage.isNotBlank()) {
            add(Icons.Default.Favorite to profile.loveLanguage)
        }
        if (profile.intention != Intention.NOT_SPECIFIED) {
            add(Icons.Default.Interests to profile.intention.getDisplayName())
        }
        if (profile.zodiacSign != ZodiacSign.NOT_SPECIFIED) {
            add(Icons.Default.Stars to profile.zodiacSign.getDisplayName())
        }
    }
    if (badges.isEmpty()) return

    Spacer(modifier = Modifier.height(10.dp))
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        maxItemsInEachRow = 3
    ) {
        badges.forEach { (icon, label) ->
            ProfileBadge(icon = icon, label = label)
        }
    }
}

@Composable
private fun ProfileBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.22f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun SwipeActionButtons(
    onPassClick: () -> Unit,
    onSuperLikeClick: () -> Unit,
    onLikeClick: () -> Unit,
    enabled: Boolean,
    onRewindClick: () -> Unit = {},
    onBoostClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botão Rewind — desfaz o último swipe
        FloatingActionButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onRewindClick()
            },
            containerColor = FypColors.Gold.copy(alpha = 0.3f),
            modifier = Modifier.size(42.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Voltar",
                tint = FypColors.Gold,
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
                tint = FypColors.Pass,
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
                tint = FypColors.SuperLike,
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
                tint = FypColors.Like,
                modifier = Modifier.size(28.dp)
            )
        }
        
        // Botão Boost — destaca o perfil por 30 min
        FloatingActionButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onBoostClick()
            },
            containerColor = FypColors.Secondary.copy(alpha = 0.3f),
            modifier = Modifier.size(42.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Boost",
                tint = FypColors.Secondary,
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
