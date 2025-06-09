package com.example.matchreal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.matchreal.ui.theme.MatchRealTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onNavigateToWaitlist: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAccessCode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo/Título
        Text(
            text = "💕 MatchReal",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Relacionamentos reais com IA inclusiva",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Status do projeto
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🎉 Jetpack Compose Configurado!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("✅ Material Design 3")
                Text("✅ Hilt Dependency Injection")
                Text("✅ Navigation Compose")
                Text("✅ Tema Rosa/Roxo personalizado")
                Text("✅ Estrutura MVVM preparada")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "🚀 Pronto para implementar a Lista de Espera!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Botões de navegação
        Button(
            onClick = onNavigateToWaitlist,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "🎯 Lista de Espera",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "🚀 Fazer Login",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        TextButton(
            onClick = onNavigateToAccessCode,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "🎟️ Tenho um código de acesso",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    MatchRealTheme {
        WelcomeScreen()
    }
} 