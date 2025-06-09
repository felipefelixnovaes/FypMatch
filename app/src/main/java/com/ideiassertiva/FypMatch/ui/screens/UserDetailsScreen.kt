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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.data.TestUsers

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserDetailsScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onLike: () -> Unit = {},
    onPass: () -> Unit = {},
    onSuperLike: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // TODO: Buscar usuário por ID - por enquanto usando dados mockados
    val user = remember {
        // Aqui você buscaria o usuário pelo ID
        TestUsers.allUsers.find { it.id == userId } ?: TestUsers.allUsers.first()
    }
    
    val pagerState = rememberPagerState(pageCount = { user.profile.photos.size })
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user.profile.fullName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            SwipeBottomBar(
                onPass = onPass,
                onSuperLike = onSuperLike,
                onLike = onLike
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Carrossel de fotos
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(user.profile.photos.getOrElse(page) { "https://picsum.photos/400/600?random=$page" })
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto ${page + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        // Indicadores de página
                        if (user.profile.photos.size > 1) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                repeat(user.profile.photos.size) { index ->
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
            
            // Informações básicas
            item {
                UserBasicInfo(user)
            }
            
            // Sobre mim
            if (user.profile.aboutMe.isNotBlank()) {
                item {
                    InfoSection(
                        title = "Sobre mim",
                        content = { Text(user.profile.aboutMe) }
                    )
                }
            }
            
            // Interesses
            if (user.profile.interests.isNotEmpty()) {
                item {
                    InfoSection(
                        title = "Interesses",
                        content = {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(user.profile.interests) { interest ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(interest) }
                                    )
                                }
                            }
                        }
                    )
                }
            }
            
            // Informações pessoais
            item {
                PersonalInfoSection(user)
            }
            
            // Gostos culturais
            item {
                CulturalPreferencesSection(user)
            }
            
            // Esportes e atividades
            if (user.profile.sports.isNotEmpty() || user.profile.hobbies.isNotEmpty()) {
                item {
                    SportsAndHobbiesSection(user)
                }
            }
            
            // Adicionar espaço no final
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun SwipeBottomBar(
    onPass: () -> Unit,
    onSuperLike: () -> Unit,
    onLike: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = onPass,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Passar",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            FloatingActionButton(
                onClick = onSuperLike,
                containerColor = Color(0xFF4FC3F7),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Super Curtir",
                    tint = Color.White
                )
            }
            
            FloatingActionButton(
                onClick = onLike,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Curtir",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun UserBasicInfo(user: User) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoChip(
                icon = Icons.Default.LocationOn,
                text = "${user.profile.location.city}, ${user.profile.location.state}"
            )
            
            InfoChip(
                icon = Icons.Default.Person,
                text = user.profile.profession
            )
            
            if (user.profile.height > 0) {
                InfoChip(
                    icon = Icons.Default.Person,
                    text = "${user.profile.height}cm"
                )
            }
        }
        
        if (user.profile.bio.isNotBlank()) {
            Text(
                text = user.profile.bio,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun PersonalInfoSection(user: User) {
    InfoSection(
        title = "Informações Pessoais"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (user.profile.relationshipStatus != RelationshipStatus.NOT_SPECIFIED) {
                InfoRow(
                    label = "Estado civil:",
                    value = user.profile.relationshipStatus.getDisplayName()
                )
            }
            
            if (user.profile.hasChildren != ChildrenStatus.NOT_SPECIFIED) {
                InfoRow(
                    label = "Tem filhos:",
                    value = user.profile.hasChildren.getDisplayName()
                )
            }
            
            if (user.profile.wantsChildren != ChildrenStatus.NOT_SPECIFIED) {
                InfoRow(
                    label = "Quer ter filhos:",
                    value = user.profile.wantsChildren.getDisplayName()
                )
            }
            
            if (user.profile.smokingStatus != SmokingStatus.NOT_SPECIFIED) {
                InfoRow(
                    label = "Fuma:",
                    value = user.profile.smokingStatus.getDisplayName()
                )
            }
            
            if (user.profile.drinkingStatus != DrinkingStatus.NOT_SPECIFIED) {
                InfoRow(
                    label = "Bebe:",
                    value = user.profile.drinkingStatus.getDisplayName()
                )
            }
            
            if (user.profile.zodiacSign != ZodiacSign.NOT_SPECIFIED) {
                InfoRow(
                    label = "Signo:",
                    value = user.profile.zodiacSign.getDisplayName()
                )
            }
            
            if (user.profile.religion != Religion.NOT_SPECIFIED) {
                InfoRow(
                    label = "Religião:",
                    value = user.profile.religion.getDisplayName()
                )
            }
            
            if (user.profile.petPreference != PetPreference.NOT_SPECIFIED) {
                InfoRow(
                    label = "Animais:",
                    value = user.profile.petPreference.getDisplayName()
                )
            }
        }
    }
}

@Composable
private fun CulturalPreferencesSection(user: User) {
    InfoSection(
        title = "Gostos e Preferências"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (user.profile.favoriteMovies.isNotEmpty()) {
                PreferenceList(
                    label = "Filmes favoritos:",
                    items = user.profile.favoriteMovies
                )
            }
            
            if (user.profile.favoriteMusic.isNotEmpty()) {
                PreferenceList(
                    label = "Música:",
                    items = user.profile.favoriteMusic
                )
            }
            
            if (user.profile.favoriteBooks.isNotEmpty()) {
                PreferenceList(
                    label = "Livros:",
                    items = user.profile.favoriteBooks
                )
            }
            
            if (user.profile.favoriteTeam.isNotBlank()) {
                InfoRow(
                    label = "Time do coração:",
                    value = user.profile.favoriteTeam
                )
            }
            
            if (user.profile.languages.isNotEmpty()) {
                PreferenceList(
                    label = "Idiomas:",
                    items = user.profile.languages
                )
            }
        }
    }
}

@Composable
private fun SportsAndHobbiesSection(user: User) {
    InfoSection(
        title = "Esportes e Hobbies"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (user.profile.sports.isNotEmpty()) {
                PreferenceList(
                    label = "Esportes:",
                    items = user.profile.sports
                )
            }
            
            if (user.profile.hobbies.isNotEmpty()) {
                PreferenceList(
                    label = "Hobbies:",
                    items = user.profile.hobbies
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PreferenceList(
    label: String,
    items: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                AssistChip(
                    onClick = { },
                    label = { Text(item, style = MaterialTheme.typography.bodySmall) }
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 
