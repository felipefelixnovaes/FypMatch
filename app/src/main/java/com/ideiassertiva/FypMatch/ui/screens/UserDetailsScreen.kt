package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.theme.FypColors
import com.ideiassertiva.FypMatch.ui.viewmodel.UserDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserDetailsScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onLike: () -> Unit = {},
    onPass: () -> Unit = {},
    onSuperLike: () -> Unit = {},
    viewModel: UserDetailsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }

    Scaffold(
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            SwipeBottomBar(onPass = onPass, onSuperLike = onSuperLike, onLike = onLike)
        }
    ) { paddingValues ->
        when {
            isLoading && user == null -> {
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
                        Card(
                            modifier = Modifier.fillMaxWidth().height(400.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box {
                                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(u.profile.photos.getOrElse(page) { "https://ui-avatars.com/api/?name={u.profile.fullName}&background=E91E63&color=fff&size=400" })
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Foto {page + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                if (u.profile.photos.size > 1) {
                                    Row(
                                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        repeat(u.profile.photos.size) { index ->
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(
                                                        if (index == pagerState.currentPage) Color.White
                                                        else Color.White.copy(alpha = 0.5f),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { UserBasicInfo(u) }

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

@Composable
private fun SwipeBottomBar(onPass: () -> Unit, onSuperLike: () -> Unit, onLike: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(onClick = onPass, containerColor = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Default.Close, "Passar", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
            FloatingActionButton(onClick = onSuperLike, containerColor = FypColors.SuperLike, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Star, "Super Curtir", tint = Color.White)
            }
            FloatingActionButton(onClick = onLike, containerColor = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Default.Favorite, "Curtir", tint = Color.White)
            }
        }
    }
}

@Composable
private fun UserBasicInfo(user: User) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${user.profile.fullName}, ${user.profile.age}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (user.subscription != SubscriptionStatus.FREE) {
                AssistChip(
                    onClick = { },
                    label = { Text(user.subscription.name) },
                    leadingIcon = { Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp)) }
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoChip(icon = Icons.Default.LocationOn, text = "${user.profile.location.city}, ${user.profile.location.state}")
            InfoChip(icon = Icons.Default.Person, text = user.profile.profession)
            if (user.profile.height > 0) {
                InfoChip(icon = Icons.Default.Person, text = "${user.profile.height}cm")
            }
        }
        if (user.profile.bio.isNotBlank()) {
            Text(text = user.profile.bio, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InfoSection(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
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
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
