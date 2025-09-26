package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Phase3DemoScreen(
    onNavigateToMockChat: (String, String) -> Unit,
    onNavigateToFirebaseChat: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text("🚀 Fase 3 - Chat em Tempo Real") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status das implementações
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "📊 Status da Implementação",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                FeatureStatusRow("💬 Chat UI Moderna", true)
                FeatureStatusRow("🔄 ViewModels MVVM", true)
                FeatureStatusRow("📱 Material Design 3", true)
                FeatureStatusRow("⚡ Firebase Integration", true, "Nova!")
                FeatureStatusRow("📲 Push Notifications", true, "Nova!")
                FeatureStatusRow("👀 Typing Indicators", true, "Nova!")
                FeatureStatusRow("✅ Message Status", true, "Nova!")
                FeatureStatusRow("🌐 Real-time Sync", true, "Nova!")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Comparação de implementações
        Text(
            "🎯 Compare as Implementações",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DemoOptionCard(
                    title = "📝 Chat Atual (Mock)",
                    description = "Implementação existente com dados simulados e respostas automáticas inteligentes",
                    features = listOf(
                        "Interface completa",
                        "Respostas automáticas",
                        "Reactions e anexos",
                        "Status offline"
                    ),
                    buttonText = "Testar Chat Mock",
                    buttonColor = MaterialTheme.colorScheme.secondary,
                    onClick = { onNavigateToMockChat("test-conversation", "user1") }
                )
            }
            
            item {
                DemoOptionCard(
                    title = "🔥 Chat Firebase (Novo)",
                    description = "Nova implementação com Firebase para mensagens em tempo real",
                    features = listOf(
                        "Sincronização em tempo real",
                        "Indicadores de digitação",
                        "Status de entrega",
                        "Notificações push",
                        "Presença online/offline",
                        "Persistência na nuvem"
                    ),
                    buttonText = "Testar Chat Firebase",
                    buttonColor = MaterialTheme.colorScheme.primary,
                    onClick = { onNavigateToFirebaseChat("firebase-conversation", "user1") },
                    isNew = true
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Próximos passos
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF3E5F5)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "🎯 Próximos Passos da Fase 3",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                listOf(
                    "Configurar Firebase com dados reais",
                    "Implementar autenticação de usuários",
                    "Adicionar upload de mídia",
                    "Configurar notificações push",
                    "Testes com usuários beta",
                    "Otimização de performance"
                ).forEach { step ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            step,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureStatusRow(
    feature: String,
    isImplemented: Boolean,
    badge: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isImplemented) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isImplemented) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            feature,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        
        badge?.let { badgeText ->
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    badgeText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun DemoOptionCard(
    title: String,
    description: String,
    features: List<String>,
    buttonText: String,
    buttonColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    isNew: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                if (isNew) {
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        color = Color(0xFF4CAF50)
                    ) {
                        Text(
                            "NOVO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "• ",
                        color = buttonColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        feature,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text(buttonText)
            }
        }
    }
}