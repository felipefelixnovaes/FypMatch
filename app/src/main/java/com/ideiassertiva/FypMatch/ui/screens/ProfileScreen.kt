package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme
import com.ideiassertiva.FypMatch.ui.viewmodel.ProfileViewModel
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToPhotoUpload: () -> Unit,
    onNavigateToDiscovery: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Estados locais para edição
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(Gender.NOT_SPECIFIED) }
    var selectedOrientation by remember { mutableStateOf(Orientation.NOT_SPECIFIED) }
    var selectedIntention by remember { mutableStateOf(Intention.NOT_SPECIFIED) }
    var showGenderDropdown by remember { mutableStateOf(false) }
    var showOrientationDropdown by remember { mutableStateOf(false) }
    var showIntentionDropdown by remember { mutableStateOf(false) }
    
    // Carregar dados do usuário quando disponível
    LaunchedEffect(uiState.user) {
        uiState.user?.let { user ->
            fullName = user.profile.fullName
            age = if (user.profile.age > 0) user.profile.age.toString() else ""
            bio = user.profile.bio
            city = user.profile.location.city
            selectedGender = user.profile.gender
            selectedOrientation = user.profile.orientation
            selectedIntention = user.profile.intention
        }
    }
    
    // Observar sucesso no salvamento
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            val user = uiState.user
            if (user != null && user.profile.photos.isEmpty()) {
                onNavigateToPhotoUpload()
            } else {
                onNavigateToDiscovery()
            }
            viewModel.clearSaveSuccess()
        }
    }
    
    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Carregando perfil...")
                }
            }
        }
        
        else -> {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "✨ Complete seu Perfil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
                    text = if (uiState.user?.profile?.fullName?.isNotBlank() == true) {
                        "Finalize seu perfil no FypMatch!"
                    } else {
                        "Vamos criar seu perfil no FypMatch!"
                    },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
                
                // Mostrar erro se houver
                uiState.error?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
        
        // Nome completo
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Nome Completo") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Idade
        OutlinedTextField(
            value = age,
            onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 2) age = it },
            label = { Text("Idade") },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Cidade
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Cidade") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gênero
        ExposedDropdownMenuBox(
            expanded = showGenderDropdown,
            onExpandedChange = { showGenderDropdown = !showGenderDropdown }
        ) {
            OutlinedTextField(
                value = selectedGender.getDisplayName(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Gênero") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGenderDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            
            ExposedDropdownMenu(
                expanded = showGenderDropdown,
                onDismissRequest = { showGenderDropdown = false }
            ) {
                Gender.values().forEach { gender ->
                    DropdownMenuItem(
                        text = { Text(gender.getDisplayName()) },
                        onClick = {
                            selectedGender = gender
                            showGenderDropdown = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Orientação
        ExposedDropdownMenuBox(
            expanded = showOrientationDropdown,
            onExpandedChange = { showOrientationDropdown = !showOrientationDropdown }
        ) {
            OutlinedTextField(
                value = selectedOrientation.getDisplayName(),
                onValueChange = {},
                readOnly = true,
                        label = { Text("Orientação") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showOrientationDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            
            ExposedDropdownMenu(
                expanded = showOrientationDropdown,
                onDismissRequest = { showOrientationDropdown = false }
            ) {
                Orientation.values().forEach { orientation ->
                    DropdownMenuItem(
                        text = { Text(orientation.getDisplayName()) },
                        onClick = {
                            selectedOrientation = orientation
                            showOrientationDropdown = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Intenção
        ExposedDropdownMenuBox(
            expanded = showIntentionDropdown,
            onExpandedChange = { showIntentionDropdown = !showIntentionDropdown }
        ) {
            OutlinedTextField(
                value = selectedIntention.getDisplayName(),
                onValueChange = {},
                readOnly = true,
                label = { Text("O que você busca?") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showIntentionDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            
            ExposedDropdownMenu(
                expanded = showIntentionDropdown,
                onDismissRequest = { showIntentionDropdown = false }
            ) {
                Intention.values().forEach { intention ->
                    DropdownMenuItem(
                        text = { Text(intention.getDisplayName()) },
                        onClick = {
                            selectedIntention = intention
                            showIntentionDropdown = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bio
        OutlinedTextField(
            value = bio,
            onValueChange = { if (it.length <= 500) bio = it },
            label = { Text("Sobre você") },
            placeholder = { Text("Conte um pouco sobre você...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4,
            supportingText = {
                Text("${bio.length}/500 caracteres")
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Botão de continuar
        Button(
            onClick = {
                        val currentUser = uiState.user
                        if (currentUser != null) {
                            val updatedUser = currentUser.copy(
                                profile = currentUser.profile.copy(
                                    fullName = fullName,
                                    age = age.toIntOrNull() ?: 18,
                                    bio = bio,
                                    location = currentUser.profile.location.copy(city = city),
                                    gender = selectedGender,
                                    orientation = selectedOrientation,
                                    intention = selectedIntention,
                                    isProfileComplete = true
                                )
                            )
                            viewModel.saveProfile(updatedUser)
                        }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
                    enabled = !uiState.isSaving && 
                             fullName.isNotBlank() && 
                     age.isNotBlank() && 
                     city.isNotBlank() && 
                     selectedGender != Gender.NOT_SPECIFIED &&
                     selectedOrientation != Orientation.NOT_SPECIFIED &&
                     selectedIntention != Intention.NOT_SPECIFIED &&
                     bio.isNotBlank()
        ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
            Text(
                            text = "📝 Continuar para Foto",
                style = MaterialTheme.typography.titleMedium
            )
                    }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Nota sobre fotos
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                        text = "📸 Próximo passo: Adicionar sua foto de perfil!",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    FypMatchTheme {
        ProfileScreen(
            onNavigateToPhotoUpload = {},
            onNavigateToDiscovery = {}
        )
    }
} 
