package com.example.matchreal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.matchreal.model.*
import com.example.matchreal.ui.theme.MatchRealTheme
import com.example.matchreal.ui.viewmodel.MatchesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    viewModel: MatchesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Seus Matches",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
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
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            matches.isEmpty() -> {
                EmptyMatchesView()
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(matches) { match ->
                        MatchCard(
                            match = match,
                            onClick = { onNavigateToChat(match.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMatchesView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "💕",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Ainda não há matches",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Continue curtindo perfis para encontrar pessoas especiais!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchCard(
    match: Match,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto do perfil
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://picsum.photos/100/100?random=${match.id.hashCode()}")
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto do match",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Informações do match
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Match #${match.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Match",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (match.lastMessage.isNotBlank()) {
                    Text(
                        text = match.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Diga olá! 👋",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatMatchDate(match.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Indicador de nova mensagem (simulado)
            if (match.lastMessage.isBlank()) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                )
            }
        }
    }
}

private fun formatMatchDate(date: Date): String {
    val now = Date()
    val diffInMillis = now.time - date.time
    val diffInHours = diffInMillis / (1000 * 60 * 60)
    val diffInDays = diffInHours / 24
    
    return when {
        diffInHours < 1 -> "Agora mesmo"
        diffInHours < 24 -> "${diffInHours}h atrás"
        diffInDays < 7 -> "${diffInDays}d atrás"
        else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
    }
}

@Preview(showBackground = true)
@Composable
fun MatchesScreenPreview() {
    MatchRealTheme {
        MatchesScreen()
    }
} 