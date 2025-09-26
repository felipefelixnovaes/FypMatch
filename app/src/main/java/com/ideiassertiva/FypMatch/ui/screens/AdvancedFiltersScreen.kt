package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme
import com.ideiassertiva.FypMatch.ui.viewmodel.FiltersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFiltersScreen(
    onNavigateBack: () -> Unit = {},
    onUpgradeToPremium: () -> Unit = {},
    currentSubscription: SubscriptionStatus = SubscriptionStatus.FREE,
    viewModel: FiltersViewModel = viewModel()
) {
    val filters by viewModel.filters.collectAsState()
    val appliedFiltersCount by viewModel.appliedFiltersCount.collectAsState()
    
    val isPremiumFeature = currentSubscription == SubscriptionStatus.FREE
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Filtros Avançados",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (appliedFiltersCount > 0) {
                        Text(
                            text = "$appliedFiltersCount filtros ativos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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
            actions = {
                if (appliedFiltersCount > 0) {
                    TextButton(
                        onClick = { viewModel.clearAllFilters() }
                    ) {
                        Text("Limpar")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Premium Notice
            if (isPremiumFeature) {
                item {
                    PremiumFeatureCard(
                        title = "Filtros Avançados",
                        description = "Encontre exatamente quem você procura com filtros detalhados",
                        onUpgrade = onUpgradeToPremium
                    )
                }
            }
            
            // Basic Filters (Available for everyone)
            item {
                FilterSection(
                    title = "Básicos",
                    icon = Icons.Default.FilterList
                ) {
                    // Age Range
                    AgeRangeFilter(
                        currentRange = filters.ageRange,
                        onRangeChange = { viewModel.updateAgeRange(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Distance
                    DistanceFilter(
                        currentDistance = filters.maxDistance,
                        onDistanceChange = { viewModel.updateMaxDistance(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Show verified only
                    FilterToggle(
                        title = "Apenas verificados",
                        description = "Mostrar apenas perfis com foto verificada",
                        checked = filters.verifiedOnly,
                        onCheckedChange = { viewModel.toggleVerifiedOnly(it) },
                        enabled = !isPremiumFeature
                    )
                }
            }
            
            // Premium Filters
            item {
                FilterSection(
                    title = "Avançados",
                    icon = Icons.Default.Star,
                    isPremium = true,
                    enabled = !isPremiumFeature
                ) {
                    // Recently Active
                    FilterToggle(
                        title = "Ativo recentemente",
                        description = "Apenas usuários ativos nas últimas 24h",
                        checked = filters.recentlyActive,
                        onCheckedChange = { viewModel.toggleRecentlyActive(it) },
                        enabled = !isPremiumFeature
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Minimum photos
                    MinPhotosFilter(
                        currentMin = filters.minPhotos,
                        onMinChange = { viewModel.updateMinPhotos(it) },
                        enabled = !isPremiumFeature
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Height Range
                    HeightRangeFilter(
                        currentRange = filters.heightRange,
                        onRangeChange = { viewModel.updateHeightRange(it) },
                        enabled = !isPremiumFeature
                    )
                }
            }
            
            // Lifestyle Filters
            item {
                FilterSection(
                    title = "Estilo de Vida",
                    icon = Icons.Default.LocalBar,
                    isPremium = true,
                    enabled = !isPremiumFeature
                ) {
                    // Smoking
                    MultiSelectFilter(
                        title = "Fumo",
                        options = SmokingStatus.values().filter { it != SmokingStatus.NOT_SPECIFIED },
                        selectedOptions = filters.smokingStatus,
                        onSelectionChange = { viewModel.updateSmokingStatus(it) },
                        getDisplayName = { it.getDisplayName() },
                        enabled = !isPremiumFeature
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Drinking
                    MultiSelectFilter(
                        title = "Bebida",
                        options = DrinkingStatus.values().filter { it != DrinkingStatus.NOT_SPECIFIED },
                        selectedOptions = filters.drinkingStatus,
                        onSelectionChange = { viewModel.updateDrinkingStatus(it) },
                        getDisplayName = { it.getDisplayName() },
                        enabled = !isPremiumFeature
                    )
                }
            }
            
            // Family & Values
            item {
                FilterSection(
                    title = "Família e Valores",
                    icon = Icons.Default.Family,
                    isPremium = true,
                    enabled = !isPremiumFeature
                ) {
                    // Has Children
                    MultiSelectFilter(
                        title = "Tem filhos",
                        options = ChildrenStatus.values().filter { it != ChildrenStatus.NOT_SPECIFIED },
                        selectedOptions = filters.hasChildren,
                        onSelectionChange = { viewModel.updateHasChildren(it) },
                        getDisplayName = { it.getDisplayName() },
                        enabled = !isPremiumFeature
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Wants Children
                    MultiSelectFilter(
                        title = "Quer filhos",
                        options = ChildrenStatus.values().filter { it != ChildrenStatus.NOT_SPECIFIED },
                        selectedOptions = filters.wantsChildren,
                        onSelectionChange = { viewModel.updateWantsChildren(it) },
                        getDisplayName = { it.getDisplayName() },
                        enabled = !isPremiumFeature
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Religion
                    MultiSelectFilter(
                        title = "Religião",
                        options = Religion.values().filter { it != Religion.NOT_SPECIFIED },
                        selectedOptions = filters.religions,
                        onSelectionChange = { viewModel.updateReligions(it) },
                        getDisplayName = { it.getDisplayName() },
                        enabled = !isPremiumFeature
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    icon: ImageVector,
    isPremium: Boolean = false,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isPremium) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (isPremium) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFF9800)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "PREMIUM",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(content = content)
        }
    }
}

@Composable
private fun PremiumFeatureCard(
    title: String,
    description: String,
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Button(
                onClick = onUpgrade,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                )
            ) {
                Text(
                    text = "Upgrade para Premium",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FilterToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun AgeRangeFilter(
    currentRange: IntRange,
    onRangeChange: (IntRange) -> Unit
) {
    Column {
        Text(
            text = "Idade: ${currentRange.first} - ${currentRange.last} anos",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        // Age range slider would be implemented here
        // For now, showing static UI
        Slider(
            value = currentRange.first.toFloat(),
            onValueChange = { /* Handle change */ },
            valueRange = 18f..99f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DistanceFilter(
    currentDistance: Int,
    onDistanceChange: (Int) -> Unit
) {
    Column {
        Text(
            text = "Distância: até $currentDistance km",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        Slider(
            value = currentDistance.toFloat(),
            onValueChange = { onDistanceChange(it.toInt()) },
            valueRange = 1f..200f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MinPhotosFilter(
    currentMin: Int,
    onMinChange: (Int) -> Unit,
    enabled: Boolean = true
) {
    Column {
        Text(
            text = "Mínimo de fotos: $currentMin",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (enabled) Color.Unspecified else Color.Gray
        )
        
        Slider(
            value = currentMin.toFloat(),
            onValueChange = { onMinChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HeightRangeFilter(
    currentRange: IntRange?,
    onRangeChange: (IntRange?) -> Unit,
    enabled: Boolean = true
) {
    Column {
        Text(
            text = if (currentRange != null) {
                "Altura: ${currentRange.first} - ${currentRange.last} cm"
            } else {
                "Altura: Qualquer"
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (enabled) Color.Unspecified else Color.Gray
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = currentRange != null,
                onCheckedChange = { 
                    if (it) {
                        onRangeChange(150..200)
                    } else {
                        onRangeChange(null)
                    }
                },
                enabled = enabled
            )
            Text("Filtrar por altura")
        }
        
        if (currentRange != null && enabled) {
            Slider(
                value = currentRange.first.toFloat(),
                onValueChange = { /* Handle change */ },
                valueRange = 140f..220f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun <T> MultiSelectFilter(
    title: String,
    options: Array<T>,
    selectedOptions: List<T>,
    onSelectionChange: (List<T>) -> Unit,
    getDisplayName: (T) -> String,
    enabled: Boolean = true
) {
    Column {
        Text(
            text = "$title (${selectedOptions.size} selecionados)",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (enabled) Color.Unspecified else Color.Gray
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(options.toList()) { option ->
                val isSelected = selectedOptions.contains(option)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (enabled) {
                            val newSelection = if (isSelected) {
                                selectedOptions - option
                            } else {
                                selectedOptions + option
                            }
                            onSelectionChange(newSelection)
                        }
                    },
                    label = { Text(getDisplayName(option)) },
                    enabled = enabled
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdvancedFiltersScreenPreview() {
    FypMatchTheme {
        AdvancedFiltersScreen(
            currentSubscription = SubscriptionStatus.PREMIUM
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AdvancedFiltersScreenFreePreview() {
    FypMatchTheme {
        AdvancedFiltersScreen(
            currentSubscription = SubscriptionStatus.FREE
        )
    }
}