package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.models.ReportReason
import com.ideiassertiva.FypMatch.ui.components.AvailabilitySummaryCard
import com.ideiassertiva.FypMatch.ui.components.BlockUserDialog
import com.ideiassertiva.FypMatch.ui.components.CompatibilityRaritySeal
import com.ideiassertiva.FypMatch.ui.components.CompatibilityRaritySealBadge
import com.ideiassertiva.FypMatch.ui.components.ReportUserDialog
import com.ideiassertiva.FypMatch.ui.theme.FypColors
import com.ideiassertiva.FypMatch.ui.viewmodel.UserDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserDetailsScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onActionComplete: () -> Unit = onNavigateBack,
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToCompatibility: (String) -> Unit = {},
    viewModel: UserDetailsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isActionLoading by viewModel.isActionLoading.collectAsStateWithLifecycle()
    val actionCompleted by viewModel.actionCompleted.collectAsStateWithLifecycle()
    val chatNavigation by viewModel.chatNavigation.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val reportSuccess by viewModel.reportSuccess.collectAsStateWithLifecycle()
    val blockSuccess by viewModel.blockSuccess.collectAsStateWithLifecycle()
    val availability by viewModel.availability.collectAsStateWithLifecycle()
    val compatibilityHighlights by viewModel.compatibilityHighlights.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showReportDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }

    LaunchedEffect(actionCompleted) {
        if (actionCompleted) {
            viewModel.clearActionCompleted()
            onActionComplete()
        }
    }

    LaunchedEffect(chatNavigation) {
        chatNavigation?.let { conversationId ->
            viewModel.clearChatNavigation()
            onNavigateToChat(conversationId)
        }
    }

    LaunchedEffect(error) {
        error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(reportSuccess) {
        if (reportSuccess) {
            snackbarHostState.showSnackbar("Usuário denunciado com sucesso")
            showReportDialog = false
            viewModel.clearReportSuccess()
        }
    }

    LaunchedEffect(blockSuccess) {
        if (blockSuccess) {
            snackbarHostState.showSnackbar("Usuário bloqueado com sucesso")
            showBlockDialog = false
            viewModel.clearBlockSuccess()
            onNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(user?.profile?.fullName ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    val username = user?.username
                    if (!username.isNullOrBlank()) {
                        val context = LocalContext.current
                        IconButton(onClick = { shareProfile(context, username) }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartilhar perfil")
                        }
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Denunciar usuário") },
                            onClick = {
                                showMenu = false
                                showReportDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Report, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Bloquear usuário") },
                            onClick = {
                                showMenu = false
                                showBlockDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            SwipeBottomBar(
                enabled = !isActionLoading && !isLoading && user != null,
                onPass = { viewModel.performSwipe(userId, SwipeType.PASS) },
                onSuperLike = { viewModel.performSwipe(userId, SwipeType.SUPER_LIKE) },
                onLike = { viewModel.performSwipe(userId, SwipeType.LIKE) }
            )
        }
    ) { paddingValues ->
        if (showReportDialog) {
            ReportUserDialog(
                onDismiss = { showReportDialog = false },
                onConfirm = { reason, description ->
                    viewModel.reportUser(userId, reason, description)
                }
            )
        }

        if (showBlockDialog) {
            BlockUserDialog(
                onDismiss = { showBlockDialog = false },
                onConfirm = {
                    viewModel.blockUser(userId)
                }
            )
        }

        when {
            (isLoading || isActionLoading) && user == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            user == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Usuário não encontrado",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                val u = user!!
                val pagerState = rememberPagerState(pageCount = { maxOf(u.profile.photos.size, 1) })

                LazyColumn(
                    modifier = modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ProfileDetailsHero(
                            user = u,
                            pagerState = pagerState,
                            compatibilityRarity = compatibilityHighlights.rarity
                        )
                    }

                    item { UserBasicInfo(u, compatibilityHighlights.rarity) }
                    item {
                        CompatibilityEntryCard(
                            rarity = compatibilityHighlights.rarity,
                            onClick = { onNavigateToCompatibility(u.id.ifBlank { userId }) }
                        )
                    }

                    availability?.let { userAvailability ->
                        item {
                            AvailabilitySummaryCard(
                                availability = userAvailability,
                                title = "Disponibilidade"
                            )
                        }
                    }

                    // Resultados dos questionários de autoconhecimento
                    if (u.profile.enneagramType.isNotBlank() ||
                        u.profile.personalityArchetype.isNotBlank() ||
                        u.profile.loveLanguage.isNotBlank()
                    ) {
                        item { SelfKnowledgeSection(u) }
                    }

                    if (u.profile.aboutMe.isNotBlank()) {
                        item {
                            InfoSection(title = "Sobre mim") { Text(u.profile.aboutMe) }
                        }
                    }

                    if (u.profile.interests.isNotEmpty()) {
                        item {
                            InfoSection(title = "Interesses") {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(u.profile.interests) { interest ->
                                        AssistChip(onClick = { }, label = { Text(interest) })
                                    }
                                }
                            }
                        }
                    }

                    item { PersonalInfoSection(u) }
                    item { CulturalPreferencesSection(u) }

                    if (u.profile.sports.isNotEmpty() || u.profile.hobbies.isNotEmpty()) {
                        item { SportsAndHobbiesSection(u) }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun ProfileDetailsHero(
    user: User,
    pagerState: PagerState,
    compatibilityRarity: CompatibilityRarity
) {
    val profile = user.profile
    val photos = profile.photos
    val shape = RoundedCornerShape(28.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(460.dp),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            photos.getOrElse(page) {
                                "https://ui-avatars.com/api/?name=${profile.fullName}&background=E91E63&color=fff&size=600"
                            }
                        )
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto ${page + 1} de ${profile.fullName}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.00f to Color.Black.copy(alpha = 0.16f),
                                0.42f to Color.Transparent,
                                0.68f to Color.Black.copy(alpha = 0.42f),
                                1.00f to Color.Black.copy(alpha = 0.90f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompatibilityRaritySeal(rarity = compatibilityRarity)
                    if (user.isPhotoVerified()) {
                        HeroStatusBadge(
                            icon = Icons.Default.VerifiedUser,
                            text = "Foto verificada"
                        )
                    }
                }

                if (photos.size > 1) {
                    HeroPhotoIndicators(
                        count = photos.size,
                        currentPage = pagerState.currentPage
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${profile.fullName}, ${profile.age}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 2
                ) {
                    if (profile.location.city.isNotBlank()) {
                        HeroInfoPill(
                            icon = Icons.Default.LocationOn,
                            text = listOf(profile.location.city, profile.location.state)
                                .filter { it.isNotBlank() }
                                .joinToString(", ")
                        )
                    }
                    if (profile.profession.isNotBlank()) {
                        HeroInfoPill(icon = Icons.Default.Work, text = profile.profession)
                    }
                    if (profile.intention != Intention.NOT_SPECIFIED) {
                        HeroInfoPill(icon = Icons.Default.FavoriteBorder, text = profile.intention.getDisplayName())
                    }
                }

                if (profile.bio.isNotBlank()) {
                    Text(
                        text = profile.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroStatusBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.38f))
            .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun HeroPhotoIndicators(count: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.34f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .size(width = if (index == currentPage) 18.dp else 6.dp, height = 6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (index == currentPage) Color.White else Color.White.copy(alpha = 0.42f)
                    )
            )
        }
    }
}

@Composable
private fun HeroInfoPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .widthIn(max = 240.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SwipeBottomBar(
    enabled: Boolean,
    onPass: () -> Unit,
    onSuperLike: () -> Unit,
    onLike: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            BottomActionItem(
                icon = Icons.Default.Close,
                label = "Não",
                color = FypColors.Pass,
                size = 56.dp,
                enabled = enabled,
                onClick = onPass
            )
            BottomActionItem(
                icon = Icons.Default.Star,
                label = "Super Like",
                color = FypColors.SuperLike,
                size = 56.dp,
                highlighted = true,
                enabled = enabled,
                onClick = onSuperLike
            )
            BottomActionItem(
                icon = Icons.Default.Favorite,
                label = "Curtir",
                color = FypColors.Primary,
                size = 56.dp,
                enabled = enabled,
                onClick = onLike
            )
        }
    }
}

@Composable
private fun BottomActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    size: androidx.compose.ui.unit.Dp,
    highlighted: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FloatingActionButton(
            onClick = { if (enabled) onClick() },
            modifier = Modifier.size(size),
            containerColor = if (!enabled) {
                MaterialTheme.colorScheme.surfaceVariant
            } else if (highlighted) {
                color
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = if (!enabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else if (highlighted) {
                Color.White
            } else {
                color
            },
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompatibilityEntryCard(
    rarity: CompatibilityRarity,
    onClick: () -> Unit
) {
    val accent = if (rarity == CompatibilityRarity.NONE) {
        MaterialTheme.colorScheme.primary
    } else {
        userDetailsRarityColor(rarity)
    }
    val shape = RoundedCornerShape(22.dp)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            accent.copy(alpha = 0.10f),
                            MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(FypColors.Primary.copy(alpha = 0.20f), RoundedCornerShape(16.dp))
                    .border(1.dp, FypColors.Primary.copy(alpha = 0.34f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = accent
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Compatibilidade com voce",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    CompatibilityRaritySeal(rarity = rarity)
                }
                Text(
                    text = "Radar de afinidade, pontos fortes e sinais para conversar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun userDetailsRarityColor(rarity: CompatibilityRarity): Color {
    return when (rarity) {
        CompatibilityRarity.BRONZE -> Color(0xFFE58B3A)
        CompatibilityRarity.SILVER -> Color(0xFF8FA8C8)
        CompatibilityRarity.GOLD -> Color(0xFFFFB300)
        CompatibilityRarity.NONE -> FypColors.Primary
    }
}

@Composable
private fun SelfKnowledgeSection(user: User) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Autoconhecimento",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (user.profile.enneagramType.isNotBlank()) {
            SelfKnowledgeCard(
                icon = Icons.Default.Psychology,
                tint = FypColors.Secondary,
                label = "Eneagrama",
                value = user.profile.enneagramType
            )
        }
        if (user.profile.personalityArchetype.isNotBlank()) {
            SelfKnowledgeCard(
                icon = Icons.Default.AutoAwesome,
                tint = FypColors.Primary,
                label = "Arquétipo de personalidade",
                value = user.profile.personalityArchetype
            )
        }
        if (user.profile.loveLanguage.isNotBlank()) {
            SelfKnowledgeCard(
                icon = Icons.Default.Favorite,
                tint = FypColors.Primary,
                label = "Linguagem do amor",
                value = user.profile.loveLanguage
            )
        }
    }
}

@Composable
private fun SelfKnowledgeCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(tint.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserBasicInfo(user: User, compatibilityRarity: CompatibilityRarity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(FypColors.Primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Resumo do perfil",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (compatibilityRarity != CompatibilityRarity.NONE) {
                    CompatibilityRaritySeal(rarity = compatibilityRarity)
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (user.profile.location.city.isNotBlank()) {
                    InfoChip(icon = Icons.Default.LocationOn, text = "${user.profile.location.city}, ${user.profile.location.state}")
                }
                if (user.profile.profession.isNotBlank()) {
                    InfoChip(icon = Icons.Default.Work, text = user.profile.profession)
                }
                if (user.profile.intention != Intention.NOT_SPECIFIED) {
                    InfoChip(icon = Icons.Default.FavoriteBorder, text = user.profile.intention.getDisplayName())
                }
                if (user.profile.height > 0) {
                    InfoChip(icon = Icons.Default.Straighten, text = "${user.profile.height} cm")
                }
                if (user.isPhotoVerified()) {
                    InfoChip(icon = Icons.Default.VerifiedUser, text = "Foto verificada")
                }
            }
        }
    }
}

@Composable
private fun InfoSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(FypColors.Primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
private fun PersonalInfoSection(user: User) {
    InfoSection(title = "Informacoes Pessoais") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (user.profile.relationshipStatus != RelationshipStatus.NOT_SPECIFIED)
                InfoRow("Estado civil:", user.profile.relationshipStatus.getDisplayName())
            if (user.profile.hasChildren != ChildrenStatus.NOT_SPECIFIED)
                InfoRow("Tem filhos:", user.profile.hasChildren.getDisplayName())
            if (user.profile.wantsChildren != ChildrenStatus.NOT_SPECIFIED)
                InfoRow("Quer ter filhos:", user.profile.wantsChildren.getDisplayName())
            if (user.profile.smokingStatus != SmokingStatus.NOT_SPECIFIED)
                InfoRow("Fuma:", user.profile.smokingStatus.getDisplayName())
            if (user.profile.drinkingStatus != DrinkingStatus.NOT_SPECIFIED)
                InfoRow("Bebe:", user.profile.drinkingStatus.getDisplayName())
            if (user.profile.zodiacSign != ZodiacSign.NOT_SPECIFIED)
                InfoRow("Signo:", user.profile.zodiacSign.getDisplayName())
            if (user.profile.religion != Religion.NOT_SPECIFIED)
                InfoRow("Religiao:", user.profile.religion.getDisplayName())
            if (user.profile.petPreference != PetPreference.NOT_SPECIFIED)
                InfoRow("Animais:", user.profile.petPreference.getDisplayName())
        }
    }
}

@Composable
private fun CulturalPreferencesSection(user: User) {
    InfoSection(title = "Gostos e Preferencias") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (user.profile.favoriteMovies.isNotEmpty()) PreferenceList("Filmes favoritos:", user.profile.favoriteMovies)
            if (user.profile.favoriteMusic.isNotEmpty()) PreferenceList("Musica:", user.profile.favoriteMusic)
            if (user.profile.favoriteBooks.isNotEmpty()) PreferenceList("Livros:", user.profile.favoriteBooks)
            if (user.profile.favoriteTeam.isNotBlank()) InfoRow("Time do coracao:", user.profile.favoriteTeam)
            if (user.profile.languages.isNotEmpty()) PreferenceList("Idiomas:", user.profile.languages)
        }
    }
}

@Composable
private fun SportsAndHobbiesSection(user: User) {
    InfoSection(title = "Esportes e Hobbies") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (user.profile.sports.isNotEmpty()) PreferenceList("Esportes:", user.profile.sports)
            if (user.profile.hobbies.isNotEmpty()) PreferenceList("Hobbies:", user.profile.hobbies)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.1f)
        )
    }
}

@Composable
private fun PreferenceList(label: String, items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { item ->
                AssistChip(onClick = { }, label = { Text(item, style = MaterialTheme.typography.bodySmall) })
            }
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = FypColors.Primary)
            Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun shareProfile(context: android.content.Context, username: String) {
    val url = "https://fypmatch.web.app/u/$username"
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, "Vem me conhecer no FypMatch! $url")
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Compartilhar perfil via"))
}
