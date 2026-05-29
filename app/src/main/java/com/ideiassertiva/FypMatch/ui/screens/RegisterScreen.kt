package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ideiassertiva.FypMatch.ui.components.FypGradientButton
import com.ideiassertiva.FypMatch.ui.components.FypTextField
import com.ideiassertiva.FypMatch.ui.theme.MatchPink40
import com.ideiassertiva.FypMatch.ui.theme.MatchPurple40
import com.ideiassertiva.FypMatch.ui.viewmodel.RegisterViewModel

/** Opções de gênero disponíveis no cadastro */
private val opcoesDéGenero = listOf("Homem", "Mulher", "Não-binário", "Prefiro não dizer")

/**
 * Tela de cadastro do FypMatch.
 * Permite criar conta com email/senha, definir nome, idade e gênero.
 */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onNavigateToDiscovery: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ─── Estado local do formulário ──────────────────────────────────────────
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var idade by remember { mutableFloatStateOf(25f) }
    var generoSelecionado by remember { mutableStateOf("") }

    // Controla se o campo já foi tocado (para mostrar erros inline)
    var nomeTocado by remember { mutableStateOf(false) }
    var emailTocado by remember { mutableStateOf(false) }
    var senhaTocada by remember { mutableStateOf(false) }
    var confirmarSenhaTocada by remember { mutableStateOf(false) }

    // Toggle de visibilidade das senhas
    var senhaVisivel by remember { mutableStateOf(false) }
    var confirmarSenhaVisivel by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // ─── Validações inline ──────────────────────────────────────────────────
    val erroNome = if (nomeTocado && nome.isBlank()) "Nome não pode ser vazio" else null
    val erroEmail = if (emailTocado && !email.contains("@")) "Email inválido" else null
    val erroSenha = if (senhaTocada && senha.length < 6) "Mínimo 6 caracteres" else null
    val erroConfirmar = if (confirmarSenhaTocada && confirmarSenha != senha) "As senhas não coincidem" else null

    val formularioValido = nome.isNotBlank()
        && email.contains("@")
        && senha.length >= 6
        && confirmarSenha == senha
        && generoSelecionado.isNotBlank()

    // Observar estado do ViewModel
    LaunchedEffect(uiState) {
        when (uiState) {
            is RegisterViewModel.RegisterUiState.Success -> onNavigateToDiscovery()
            is RegisterViewModel.RegisterUiState.Error -> {
                val msg = (uiState as RegisterViewModel.RegisterUiState.Error).message
                snackbarHostState.showSnackbar(msg)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ─── Header com gradiente ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(MatchPink40, MatchPurple40))
                    )
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "💕",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Criar conta",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "É grátis e leva menos de 2 minutos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // ─── Card do formulário ──────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Nome completo
                    FypTextField(
                        value = nome,
                        onValueChange = {
                            nome = it
                            nomeTocado = true
                        },
                        label = "Nome completo",
                        errorMessage = erroNome,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Email
                    FypTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailTocado = true
                        },
                        label = "Email",
                        keyboardType = KeyboardType.Email,
                        errorMessage = erroEmail,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Senha
                    FypTextField(
                        value = senha,
                        onValueChange = {
                            senha = it
                            senhaTocada = true
                        },
                        label = "Senha",
                        isPassword = true,
                        errorMessage = erroSenha,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Confirmar senha
                    FypTextField(
                        value = confirmarSenha,
                        onValueChange = {
                            confirmarSenha = it
                            confirmarSenhaTocada = true
                        },
                        label = "Confirmar senha",
                        isPassword = true,
                        errorMessage = erroConfirmar,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Slider de idade
                    Column {
                        Text(
                            text = "Idade: ${idade.toInt()} anos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = idade,
                            onValueChange = { idade = it },
                            valueRange = 18f..60f,
                            steps = 41,
                            colors = SliderDefaults.colors(
                                thumbColor = MatchPink40,
                                activeTrackColor = MatchPink40
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("18", style = MaterialTheme.typography.labelSmall)
                            Text("60", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Chips de gênero
                    Column {
                        Text(
                            text = "Gênero",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            opcoesDéGenero.take(2).forEach { opcao ->
                                FilterChip(
                                    selected = generoSelecionado == opcao,
                                    onClick = { generoSelecionado = opcao },
                                    label = { Text(opcao, style = MaterialTheme.typography.labelMedium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MatchPink40,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            opcoesDéGenero.drop(2).forEach { opcao ->
                                FilterChip(
                                    selected = generoSelecionado == opcao,
                                    onClick = { generoSelecionado = opcao },
                                    label = { Text(opcao, style = MaterialTheme.typography.labelMedium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MatchPink40,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // ─── Botão principal ─────────────────────────────────────────────
            FypGradientButton(
                text = "Criar conta",
                onClick = {
                    viewModel.register(
                        email = email,
                        password = senha,
                        displayName = nome,
                        age = idade.toInt(),
                        gender = generoSelecionado
                    )
                },
                enabled = formularioValido,
                isLoading = uiState is RegisterViewModel.RegisterUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Termos
            Text(
                text = "Ao criar uma conta você concorda com nossos Termos de Uso e Política de Privacidade.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Link para login
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = "Já tenho conta? ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Fazer login",
                    color = MatchPink40,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
