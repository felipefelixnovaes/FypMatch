package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.ui.components.LocationPickerField
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme
import com.ideiassertiva.FypMatch.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToDiscovery: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var location by remember { mutableStateOf(Location()) }
    var selectedGender by remember { mutableStateOf(Gender.NOT_SPECIFIED) }
    var selectedOrientation by remember { mutableStateOf(Orientation.NOT_SPECIFIED) }
    var selectedIntention by remember { mutableStateOf(Intention.NOT_SPECIFIED) }
    var showGenderDropdown by remember { mutableStateOf(false) }
    var showOrientationDropdown by remember { mutableStateOf(false) }
    var showIntentionDropdown by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            onNavigateToDiscovery()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Complete seu Perfil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Vamos criar seu perfil no FypMatch!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Nome Completo") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        LocationPickerField(
            location = location,
            onLocationChange = { location = it },
            label = "Cidade",
            supportingText = "Digite e escolha na lista ou use sua localização atual.",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showGenderDropdown,
                onDismissRequest = { showGenderDropdown = false }
            ) {
                Gender.values().forEach { gender ->
                    DropdownMenuItem(
                        text = { Text(gender.getDisplayName()) },
                        onClick = { selectedGender = gender; showGenderDropdown = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = showOrientationDropdown,
            onExpandedChange = { showOrientationDropdown = !showOrientationDropdown }
        ) {
            OutlinedTextField(
                value = selectedOrientation.getDisplayName(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Orientação Sexual") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showOrientationDropdown) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showOrientationDropdown,
                onDismissRequest = { showOrientationDropdown = false }
            ) {
                Orientation.values().forEach { orientation ->
                    DropdownMenuItem(
                        text = { Text(orientation.getDisplayName()) },
                        onClick = { selectedOrientation = orientation; showOrientationDropdown = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = showIntentionDropdown,
            onExpandedChange = { showIntentionDropdown = !showIntentionDropdown }
        ) {
            OutlinedTextField(
                value = selectedIntention.getDisplayName(),
                onValueChange = {},
                readOnly = true,
                label = { Text("O que voce busca?") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showIntentionDropdown) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showIntentionDropdown,
                onDismissRequest = { showIntentionDropdown = false }
            ) {
                Intention.values().forEach { intention ->
                    DropdownMenuItem(
                        text = { Text(intention.getDisplayName()) },
                        onClick = { selectedIntention = intention; showIntentionDropdown = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = bio,
            onValueChange = { if (it.length <= 500) bio = it },
            label = { Text("Sobre voce") },
            placeholder = { Text("Conte um pouco sobre voce...") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 4,
            supportingText = { Text("{bio.length}/500 caracteres") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(8.dp))
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.saveProfile(
                    fullName = fullName,
                    age = age,
                    location = location,
                    bio = bio,
                    gender = selectedGender,
                    orientation = selectedOrientation,
                    intention = selectedIntention
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !uiState.isLoading &&
                     fullName.isNotBlank() &&
                     age.isNotBlank() &&
                     location.city.isNotBlank() &&
                     selectedGender != Gender.NOT_SPECIFIED &&
                     selectedOrientation != Orientation.NOT_SPECIFIED &&
                     selectedIntention != Intention.NOT_SPECIFIED &&
                     bio.isNotBlank()
        ) {
            Text(
                text = "Comecar a Usar o FypMatch",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text(
                text = "Voce podera adicionar fotos na proxima versao!",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    FypMatchTheme {
        ProfileScreen(onNavigateToDiscovery = {})
    }
}
