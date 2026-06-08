package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ideiassertiva.FypMatch.model.User
import com.ideiassertiva.FypMatch.ui.theme.FypColors
import com.ideiassertiva.FypMatch.ui.viewmodel.LikesViewModel

private enum class LikesTab(val title: String) {
    RECEIVED("Recebidas"),
    SENT("Enviadas"),
    MATCHES("Matches")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserDetails: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    viewModel: LikesViewModel = hiltViewModel()
) {
    val received by viewModel.received.collectAsStateWithLifecycle()
    val sent by viewModel.sent.collectAsStateWithLifecycle()
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val chatNavigation by viewModel.chatNavigation.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(LikesTab.RECEIVED) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(chatNavigation) {
        chatNavigation?.let { conversationId ->
            onNavigateToChat(conversationId)
            viewModel.clearChatNavigation()
        }
    }

    LaunchedEffect(error) {
        error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.load()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Curtidas & Matches", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                LikesTab.values().forEach { tab ->
                    val count = when (tab) {
                        LikesTab.RECEIVED -> received.size
                        LikesTab.SENT -> sent.size
                        LikesTab.MATCHES -> matches.size
                    }
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(if (count > 0) "${tab.title} ($count)" else tab.title) }
                    )
                }
            }

            val list = when (selectedTab) {
                LikesTab.RECEIVED -> received
                LikesTab.SENT -> sent
                LikesTab.MATCHES -> matches
            }

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                list.isEmpty() -> {
                    EmptyLikesState(tab = selectedTab)
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(list) { user ->
                            ProfileGridCard(
                                user = user,
                                isMatch = selectedTab == LikesTab.MATCHES,
                                onClick = {
                                    if (selectedTab == LikesTab.MATCHES) {
                                        viewModel.openMatchChat(user.id)
                                    } else {
                                        onNavigateToUserDetails(user.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileGridCard(
    user: User,
    isMatch: Boolean,
    onClick: () -> Unit
) {
    val photo = user.profile.photos.firstOrNull()
        ?: "https://ui-avatars.com/api/?name=${user.profile.fullName}&background=E91E63&color=fff&size=400"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(photo).crossfade(true).build(),
                contentDescription = user.profile.fullName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradiente para legibilidade do nome
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 250f
                        )
                    )
            )
            if (isMatch) {
                Surface(
                    color = FypColors.Primary,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Text(
                        "Match",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(
                    text = "${user.profile.fullName.substringBefore(" ")}, ${user.profile.age}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (user.profile.location.city.isNotBlank()) {
                    Text(
                        text = user.profile.location.city,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLikesState(tab: LikesTab) {
    val message = when (tab) {
        LikesTab.RECEIVED -> "Ainda ninguém te curtiu.\nContinue aparecendo no Discovery!"
        LikesTab.SENT -> "Você ainda não curtiu ninguém.\nDê o primeiro passo!"
        LikesTab.MATCHES -> "Nenhum match ainda.\nQuando rolar, aparece aqui 💕"
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = FypColors.Primary.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
