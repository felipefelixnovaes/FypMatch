package com.example.matchreal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.matchreal.model.*
import com.example.matchreal.ui.theme.MatchRealTheme
import com.example.matchreal.ui.viewmodel.WaitlistScreen
import com.example.matchreal.ui.viewmodel.WaitlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaitlistScreen(
    viewModel: WaitlistViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    when (uiState.currentScreen) {
        WaitlistScreen.FORM -> {
            WaitlistFormScreen(
                viewModel = viewModel,
                uiState = uiState,
                modifier = modifier
            )
        }
        WaitlistScreen.SUCCESS -> {
            WaitlistSuccessScreen(
                user = currentUser,
                onContinue = { viewModel.navigateToDashboard() },
                modifier = modifier
            )
        }
        WaitlistScreen.DASHBOARD -> {
            WaitlistDashboardScreen(
                user = currentUser,
                stats = stats,
                onShare = { viewModel.navigateToShare() },
                modifier = modifier
            )
        }
        WaitlistScreen.SHARE -> {
            WaitlistShareScreen(
                user = currentUser,
                onBack = { viewModel.navigateToDashboard() },
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WaitlistFormScreen(
    viewModel: WaitlistViewModel,
    uiState: com.example.matchreal.ui.viewmodel.WaitlistUiState,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(Gender.NOT_SPECIFIED) }
    var selectedOrientation by remember { mutableStateOf(Orientation.NOT_SPECIFIED) }
    var selectedIntention by remember { mutableStateOf(Intention.NOT_SPECIFIED) }
    var inviteCode by remember { mutableStateOf("") }
    var showInviteField by remember { mutableStateOf(false) }
    var showGenderDropdown by remember { mutableStateOf(false) }
    var showOrientationDropdown by remember { mutableStateOf(false) }
    var showIntentionDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "🎉 Junte-se à Lista de Espera",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Seja um dos primeiros a experimentar o MatchReal!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Benefícios Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "✨ Você terá:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("🚀 Acesso antecipado garantido")
                Text("🎁 Benefícios exclusivos por indicações")
                Text("💕 Primeira experiência com IA inclusiva")
                Text("🏆 Status de Early Adopter")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Form Fields
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
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Cidade") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            OutlinedTextField(
                value = state,
                onValueChange = { state = it },
                label = { Text("Estado") },
                modifier = Modifier.weight(0.6f),
                singleLine = true
            )
        }
        
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
                    .menuAnchor()
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
                label = { Text("Orientação Sexual") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showOrientationDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
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
                    .menuAnchor()
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
        
        // Toggle para código de convite
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = showInviteField,
                onCheckedChange = { showInviteField = it }
            )
            Text(
                text = "Tenho um código de convite",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        if (showInviteField) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = inviteCode,
                onValueChange = { inviteCode = it.uppercase() },
                label = { Text("Código de Convite (opcional)") },
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = {
                    if (inviteCode.isNotEmpty() && !viewModel.validateInviteCode(inviteCode)) {
                        Text(
                            text = "Código de convite inválido",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Botão de cadastro
        Button(
            onClick = {
                val validation = viewModel.validateForm(fullName, email, city, state, age, selectedGender, selectedOrientation, selectedIntention)
                if (validation.isValid) {
                    viewModel.joinWaitlist(
                        fullName = fullName,
                        email = email,
                        city = city,
                        state = state,
                        age = age.toIntOrNull() ?: 18,
                        gender = selectedGender,
                        orientation = selectedOrientation,
                        intention = selectedIntention,
                        inviteCode = if (showInviteField) inviteCode else null
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "🚀 Entrar na Lista de Espera",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        
        // Error message
        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
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
    }
}

@Preview(showBackground = true)
@Composable
fun WaitlistFormScreenPreview() {
    MatchRealTheme {
        // Preview seria complexo com ViewModel, então vamos criar um preview simples
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎉 Junte-se à Lista de Espera",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
} 