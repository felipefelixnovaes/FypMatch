package com.example.matchreal.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.matchreal.model.isProfileComplete
import com.example.matchreal.ui.theme.MatchRealTheme
import com.example.matchreal.ui.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToDiscovery: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = run {
        val context = LocalContext.current
        viewModel { LoginViewModel(context) }
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    
    // Launcher para Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.signInWithGoogle(account)
            } catch (e: ApiException) {
                viewModel.handleSignInError("Erro no login: ${e.message}")
            }
        }
    }
    
    // Navegar automaticamente se usuário já estiver logado
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (user.isProfileComplete()) {
                onNavigateToDiscovery()
            } else {
                onNavigateToProfile()
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo e título
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "💕 MatchReal",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Relacionamentos reais com IA inclusiva",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Benefícios
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "✨ Por que escolher o MatchReal?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("🤖 IA para compatibilidade real")
                Text("🌈 Assistente para neurodiversidade")
                Text("🏆 Sistema de verificação inclusivo")
                Text("💕 Relacionamentos autênticos")
                Text("🔒 Privacidade e segurança")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Botão de login com Google
        Button(
            onClick = {
                val signInIntent = viewModel.getGoogleSignInIntent()
                googleSignInLauncher.launch(signInIntent)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ícone do Google (seria melhor usar o ícone real)
                    Text(
                        text = "G",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continuar com Google",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Termos e privacidade
        Text(
            text = "Ao continuar, você concorda com nossos Termos de Uso e Política de Privacidade",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Mensagem de erro
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
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MatchRealTheme {
        LoginScreen(
            onNavigateToProfile = {},
            onNavigateToDiscovery = {}
        )
    }
} 