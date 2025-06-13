package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.viewmodel.ProfileViewModel
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onNavigateBack: () -> Unit,
    onSave: (User) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeletePhotoDialog by remember { mutableStateOf<Int?>(null) }
    
    // Observar mudanças no estado
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSave(uiState.user!!)
            viewModel.clearSaveSuccess()
        }
    }
    
    // Mostrar erro se houver
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Aqui você pode mostrar um Snackbar ou Toast
            // Por enquanto, apenas limpar o erro após um tempo
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { 
                            uiState.user?.let { user ->
                                viewModel.saveProfile(user)
                            }
                        },
                        enabled = !uiState.isSaving && uiState.user != null
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                        Text("Salvar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
            
            uiState.user == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = uiState.error ?: "Perfil não encontrado",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { viewModel.loadCurrentUser() }) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }
            
            else -> {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                    // Mostrar erro se houver
                    uiState.error?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    
            // Seção de fotos
            PhotoSection(
                        photos = uiState.user!!.profile.photos,
                onAddPhoto = { url ->
                            viewModel.addPhoto(url)
                },
                onDeletePhoto = { index ->
                    showDeletePhotoDialog = index
                }
            )
            
            // Informações básicas
            BasicInfoSection(
                        user = uiState.user!!,
                        onUserUpdate = { updatedUser -> 
                            viewModel.updateUser(updatedUser)
                        }
            )
            
            // Sobre mim
            AboutMeSection(
                        aboutMe = uiState.user!!.profile.aboutMe,
                        bio = uiState.user!!.profile.bio,
                onAboutMeChange = { newAboutMe ->
                            val updatedUser = uiState.user!!.copy(
                                profile = uiState.user!!.profile.copy(aboutMe = newAboutMe)
                    )
                            viewModel.updateUser(updatedUser)
                },
                onBioChange = { newBio ->
                            val updatedUser = uiState.user!!.copy(
                                profile = uiState.user!!.profile.copy(bio = newBio)
                    )
                            viewModel.updateUser(updatedUser)
                }
            )
            
            // Interesses
            InterestsSection(
                        interests = uiState.user!!.profile.interests,
                onInterestsChange = { newInterests ->
                            val updatedUser = uiState.user!!.copy(
                                profile = uiState.user!!.profile.copy(interests = newInterests)
                    )
                            viewModel.updateUser(updatedUser)
                }
            )
            
            // Informações pessoais
            PersonalInfoEditSection(
                        user = uiState.user!!,
                        onUserUpdate = { updatedUser -> 
                            viewModel.updateUser(updatedUser)
                        }
            )
            
            // Preferências culturais
            CulturalPreferencesEditSection(
                        user = uiState.user!!,
                        onUserUpdate = { updatedUser -> 
                            viewModel.updateUser(updatedUser)
                        }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
    
    // Dialog para confirmar exclusão de foto
    if (showDeletePhotoDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeletePhotoDialog = null },
            title = { Text("Excluir foto") },
            text = { Text("Tem certeza que deseja excluir esta foto?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val index = showDeletePhotoDialog!!
                        viewModel.removePhoto(index)
                        showDeletePhotoDialog = null
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePhotoDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PhotoSection(
    photos: List<String>,
    onAddPhoto: (String) -> Unit,
    onDeletePhoto: (Int) -> Unit
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
                text = "Fotos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(photos.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(photos[index])
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        IconButton(
                            onClick = { onDeletePhoto(index) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(
                                    Color.Black.copy(alpha = 0.6f),
                                    RoundedCornerShape(4.dp)
                                )
                                .size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Excluir foto",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                if (photos.size < 6) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                .clickable {
                                    // TODO: Implementar seleção de foto
                                    onAddPhoto("https://picsum.photos/400/600?random=${photos.size}")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Adicionar foto",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Adicionar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            
            Text(
                text = "Adicione até 6 fotos para mostrar sua personalidade",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BasicInfoSection(
    user: User,
    onUserUpdate: (User) -> Unit
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
                text = "Informações Básicas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = user.profile.fullName,
                onValueChange = { newName ->
                    onUserUpdate(
                        user.copy(
                            profile = user.profile.copy(fullName = newName)
                        )
                    )
                },
                label = { Text("Nome completo") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = user.profile.age.toString(),
                onValueChange = { newAge ->
                    val age = newAge.toIntOrNull() ?: user.profile.age
                    onUserUpdate(
                        user.copy(
                            profile = user.profile.copy(age = age)
                        )
                    )
                },
                label = { Text("Idade") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = user.profile.profession,
                onValueChange = { newProfession ->
                    onUserUpdate(
                        user.copy(
                            profile = user.profile.copy(profession = newProfession)
                        )
                    )
                },
                label = { Text("Profissão") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = if (user.profile.height > 0) user.profile.height.toString() else "",
                onValueChange = { newHeight ->
                    val height = newHeight.toIntOrNull() ?: 0
                    onUserUpdate(
                        user.copy(
                            profile = user.profile.copy(height = height)
                        )
                    )
                },
                label = { Text("Altura (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AboutMeSection(
    aboutMe: String,
    bio: String,
    onAboutMeChange: (String) -> Unit,
    onBioChange: (String) -> Unit
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
                text = "Sobre Você",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = bio,
                onValueChange = onBioChange,
                label = { Text("Bio (descrição curta)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
            
            OutlinedTextField(
                value = aboutMe,
                onValueChange = onAboutMeChange,
                label = { Text("Sobre mim (descrição detalhada)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 6
            )
        }
    }
}

@Composable
private fun InterestsSection(
    interests: List<String>,
    onInterestsChange: (List<String>) -> Unit
) {
    var newInterest by remember { mutableStateOf("") }
    
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
                text = "Interesses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newInterest,
                    onValueChange = { newInterest = it },
                    label = { Text("Adicionar interesse") },
                    modifier = Modifier.weight(1f)
                )
                
                Button(
                    onClick = {
                        if (newInterest.isNotBlank() && newInterest !in interests) {
                            onInterestsChange(interests + newInterest.trim())
                            newInterest = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar"
                    )
                }
            }
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(interests) { interest ->
                    FilterChip(
                        onClick = {
                            onInterestsChange(interests - interest)
                        },
                        label = { Text(interest) },
                        selected = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remover",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalInfoEditSection(
    user: User,
    onUserUpdate: (User) -> Unit
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
                text = "Informações Pessoais",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Estado civil
            DropdownMenuField(
                label = "Estado civil",
                value = user.profile.relationshipStatus.getDisplayName(),
                options = RelationshipStatus.values().map { it.getDisplayName() },
                onSelectionChange = { selectedDisplayName ->
                    val selected = RelationshipStatus.values().find { 
                        it.getDisplayName() == selectedDisplayName 
                    } ?: RelationshipStatus.NOT_SPECIFIED
                    onUserUpdate(
                        user.copy(
                            profile = user.profile.copy(relationshipStatus = selected)
                        )
                    )
                }
            )
            
            // Tem filhos
            DropdownMenuField(
                label = "Tem filhos",
                value = user.profile.hasChildren.getDisplayName(),
                options = ChildrenStatus.values().map { it.getDisplayName() },
                onSelectionChange = { selectedDisplayName ->
                    val selected = ChildrenStatus.values().find { 
                        it.getDisplayName() == selectedDisplayName 
                    } ?: ChildrenStatus.NOT_SPECIFIED
                    onUserUpdate(
                        user.copy(
                            profile = user.profile.copy(hasChildren = selected)
                        )
                    )
                }
            )
            
            // Quer ter filhos
            DropdownMenuField(
                label = "Quer ter filhos",
                value = user.profile.wantsChildren.getDisplayName(),
                options = ChildrenStatus.values().map { it.getDisplayName() },
                onSelectionChange = { selectedDisplayName ->
                    val selected = ChildrenStatus.values().find { 
                        it.getDisplayName() == selectedDisplayName 
                    } ?: ChildrenStatus.NOT_SPECIFIED
                    onUserUpdate(
                        user.copy(
                            profile = user.profile.copy(wantsChildren = selected)
                        )
                    )
                }
            )
            
            // Fuma
            DropdownMenuField(
                label = "Fuma",
                value = user.profile.smokingStatus.getDisplayName(),
                options = SmokingStatus.values().map { it.getDisplayName() },
                onSelectionChange = { selectedDisplayName ->
                    val selected = SmokingStatus.values().find { 
                        it.getDisplayName() == selectedDisplayName 
                    } ?: SmokingStatus.NOT_SPECIFIED
                    onUserUpdate(
                        user.copy(
                            profile = user.profile.copy(smokingStatus = selected)
                        )
                    )
                }
            )
            
            // Bebe
            DropdownMenuField(
                label = "Bebe",
                value = user.profile.drinkingStatus.getDisplayName(),
                options = DrinkingStatus.values().map { it.getDisplayName() },
                onSelectionChange = { selectedDisplayName ->
                    val selected = DrinkingStatus.values().find { 
                        it.getDisplayName() == selectedDisplayName 
                    } ?: DrinkingStatus.NOT_SPECIFIED
                    onUserUpdate(
                        user.copy(
                            profile = user.profile.copy(drinkingStatus = selected)
                        )
                    )
                }
            )
            
            // Signo
            DropdownMenuField(
                label = "Signo",
                value = user.profile.zodiacSign.getDisplayName(),
                options = ZodiacSign.values().map { it.getDisplayName() },
                onSelectionChange = { selectedDisplayName ->
                    val selected = ZodiacSign.values().find { 
                        it.getDisplayName() == selectedDisplayName 
                    } ?: ZodiacSign.NOT_SPECIFIED
                    onUserUpdate(
                        user.copy(
                            profile = user.profile.copy(zodiacSign = selected)
                        )
                    )
                }
            )
            
            // Religião
            DropdownMenuField(
                label = "Religião",
                value = user.profile.religion.getDisplayName(),
                options = Religion.values().map { it.getDisplayName() },
                onSelectionChange = { selectedDisplayName ->
                    val selected = Religion.values().find { 
                        it.getDisplayName() == selectedDisplayName 
                    } ?: Religion.NOT_SPECIFIED
                    onUserUpdate(
                        user.copy(
                            profile = user.profile.copy(religion = selected)
                        )
                    )
                }
            )
            
            OutlinedTextField(
                value = user.profile.favoriteTeam,
                onValueChange = { newTeam ->
                    onUserUpdate(
                        user.copy(
                            profile = user.profile.copy(favoriteTeam = newTeam)
                        )
                    )
                },
                label = { Text("Time do coração") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CulturalPreferencesEditSection(
    user: User,
    onUserUpdate: (User) -> Unit
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
                text = "Preferências e Gostos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = user.profile.favoriteTeam,
                onValueChange = { newTeam ->
                    onUserUpdate(
                        user.copy(
                            profile = user.profile.copy(favoriteTeam = newTeam)
                        )
                    )
                },
                label = { Text("Time do coração") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "Adicione mais informações sobre seus gostos nas próximas atualizações do app!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownMenuField(
    label: String,
    value: String,
    options: List<String>,
    onSelectionChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectionChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
} 
