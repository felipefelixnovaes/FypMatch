package com.ideiassertiva.FypMatch.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.ideiassertiva.FypMatch.ui.theme.FypColors
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.viewmodel.ProfileEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onNavigateBack: () -> Unit,
    onSave: (User) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Inicializa o estado local com o usuário do ViewModel quando carregado
    var user by remember(uiState.user) {
        mutableStateOf(
            uiState.user ?: User()
        )
    }

    var showDeletePhotoDialog by remember { mutableStateOf<Int?>(null) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { selectedUri ->
            user = user.copy(
                profile = user.profile.copy(
                    photos = user.profile.photos + selectedUri.toString()
                )
            )
        }
    }

    // Navegar de volta após salvar com sucesso
    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            onNavigateBack()
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
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp)
                        )
                    } else {
                        TextButton(
                            onClick = {
                                viewModel.saveUser(user)
                            }
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.user == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.error?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Seção de fotos
                PhotoSection(
                    photos = user.profile.photos,
                    onAddPhoto = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onDeletePhoto = { index ->
                        showDeletePhotoDialog = index
                    }
                )

                // Informações básicas
                BasicInfoSection(
                    user = user,
                    onUserUpdate = { updatedUser -> user = updatedUser }
                )

                // Sobre mim
                AboutMeSection(
                    aboutMe = user.profile.aboutMe,
                    bio = user.profile.bio,
                    onAboutMeChange = { newAboutMe ->
                        user = user.copy(
                            profile = user.profile.copy(aboutMe = newAboutMe)
                        )
                    },
                    onBioChange = { newBio ->
                        user = user.copy(
                            profile = user.profile.copy(bio = newBio)
                        )
                    }
                )

                // Interesses
                InterestsSection(
                    interests = user.profile.interests,
                    onInterestsChange = { newInterests ->
                        user = user.copy(
                            profile = user.profile.copy(interests = newInterests)
                        )
                    }
                )

                // Informações pessoais
                PersonalInfoEditSection(
                    user = user,
                    onUserUpdate = { updatedUser -> user = updatedUser }
                )

                // Preferências culturais
                CulturalPreferencesEditSection(
                    user = user,
                    onUserUpdate = { updatedUser -> user = updatedUser }
                )

                Spacer(modifier = Modifier.height(32.dp))
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
                        user = user.copy(
                            profile = user.profile.copy(
                                photos = user.profile.photos.toMutableList().apply {
                                    removeAt(index)
                                }
                            )
                        )
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
private fun SectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    title: String,
    trailing: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(tint.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PhotoSection(
    photos: List<String>,
    onAddPhoto: () -> Unit,
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
            SectionTitle(
                icon = Icons.Default.PhotoCamera,
                tint = FypColors.Primary,
                title = "Fotos",
                trailing = "${photos.size}/6 fotos"
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
                                .clickable { onAddPhoto() },
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
            SectionTitle(
                icon = Icons.Default.Person,
                tint = FypColors.Secondary,
                title = "Informações Básicas"
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
            SectionTitle(
                icon = Icons.Default.EditNote,
                tint = FypColors.Secondary,
                title = "Sobre Você"
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { if (it.length <= 200) onBioChange(it) },
                label = { Text("Bio (descrição curta)") },
                placeholder = { Text("Fale um pouco sobre você, seus hobbies e o que procura.") },
                supportingText = {
                    Text(
                        text = "${bio.length}/200",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            OutlinedTextField(
                value = aboutMe,
                onValueChange = { if (it.length <= 500) onAboutMeChange(it) },
                label = { Text("Sobre mim (descrição detalhada)") },
                supportingText = {
                    Text(
                        text = "${aboutMe.length}/500",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 6
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestsSection(
    interests: List<String>,
    onInterestsChange: (List<String>) -> Unit
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
            SectionTitle(
                icon = Icons.Default.Interests,
                tint = FypColors.Primary,
                title = "Interesses",
                trailing = "${interests.size} selecionados"
            )

            com.ideiassertiva.FypMatch.data.InterestCatalog.categories.forEach { (categoria, opcoes) ->
                Text(
                    text = categoria,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    opcoes.forEach { opcao ->
                        val selecionado = opcao in interests
                        FilterChip(
                            selected = selecionado,
                            onClick = {
                                onInterestsChange(
                                    if (selecionado) interests - opcao else interests + opcao
                                )
                            },
                            label = { Text(opcao) },
                            leadingIcon = if (selecionado) {
                                {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
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
            SectionTitle(
                icon = Icons.Default.Badge,
                tint = FypColors.Secondary,
                title = "Informações Pessoais"
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
            SectionTitle(
                icon = Icons.Default.Favorite,
                tint = FypColors.Primary,
                title = "Preferências e Gostos"
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
                .menuAnchor()
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
