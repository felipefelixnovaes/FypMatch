package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.components.EmptyState
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme
import com.ideiassertiva.FypMatch.ui.viewmodel.PhotoManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoManagerScreen(
    onNavigateBack: () -> Unit = {},
    userId: String = "",
    currentSubscription: SubscriptionStatus = SubscriptionStatus.FREE,
    onUpgradeToPremium: () -> Unit = {},
    viewModel: PhotoManagerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    
    val uploadLimits = PhotoUploadLimits.forSubscription(currentSubscription)
    
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            viewModel.loadUserPhotos(userId)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Minhas Fotos",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${photos.size}/${uploadLimits.maxPhotos} fotos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
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
        
        if (photos.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Image,
                title = "Nenhuma foto adicionada",
                description = "Adicione pelo menos 2 fotos para ter um perfil atraente.",
                actionLabel = "Adicionar foto",
                onAction = { viewModel.addPhoto(userId) }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (photos.size < uploadLimits.maxPhotos) {
                    item {
                        AddPhotoCard(
                            subscription = currentSubscription,
                            onAddPhoto = {
                                if (currentSubscription == SubscriptionStatus.FREE && photos.size >= 6) {
                                    onUpgradeToPremium()
                                } else {
                                    viewModel.addPhoto(userId)
                                }
                            },
                            isUploading = isUploading
                        )
                    }
                }
                
                items(photos) { photo ->
                    PhotoCard(
                        photo = photo,
                        onSetAsMain = { viewModel.setAsMainPhoto(userId, photo.id) },
                        onDelete = { viewModel.deletePhoto(userId, photo.id) },
                        subscription = currentSubscription
                    )
                }
                
                if (photos.size < uploadLimits.maxPhotos) {
                    val emptySlots = uploadLimits.maxPhotos - photos.size - 1
                    repeat(emptySlots) {
                        item { EmptyPhotoSlot() }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddPhotoCard(
    subscription: SubscriptionStatus,
    onAddPhoto: () -> Unit,
    isUploading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(enabled = !isUploading) { onAddPhoto() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar foto",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Adicionar",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    if (subscription != SubscriptionStatus.FREE) {
                        Text(
                            text = "HD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoCard(
    photo: ProfilePhoto,
    onSetAsMain: () -> Unit,
    onDelete: () -> Unit,
    subscription: SubscriptionStatus
) {
    var showOptions by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = photo.url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            if (photo.isMain) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Principal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            if (subscription != SubscriptionStatus.FREE && photo.isHighQuality()) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Alta qualidade",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(16.dp)
                )
            }
            
            IconButton(
                onClick = { showOptions = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opções",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f),
                        RoundedCornerShape(bottomEnd = 8.dp)
                    )
            )
        }
    }
    
    DropdownMenu(
        expanded = showOptions,
        onDismissRequest = { showOptions = false }
    ) {
        if (!photo.isMain) {
            DropdownMenuItem(
                text = { Text("Definir como principal") },
                onClick = {
                    onSetAsMain()
                    showOptions = false
                },
                leadingIcon = {
                    Icon(Icons.Default.Star, contentDescription = null)
                }
            )
        }
        
        DropdownMenuItem(
            text = { Text("Excluir permanentemente", color = MaterialTheme.colorScheme.error) },
            onClick = {
                onDelete()
                showOptions = false
            },
            leadingIcon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        )
    }
}

@Composable
private fun EmptyPhotoSlot() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PhotoManagerScreenPreview() {
    FypMatchTheme {
        PhotoManagerScreen(currentSubscription = SubscriptionStatus.PREMIUM)
    }
}

@Preview(showBackground = true)
@Composable
fun PhotoManagerScreenFreePreview() {
    FypMatchTheme {
        PhotoManagerScreen(currentSubscription = SubscriptionStatus.FREE)
    }
}