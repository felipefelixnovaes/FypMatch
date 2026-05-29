package com.ideiassertiva.FypMatch.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.ideiassertiva.FypMatch.ui.components.PremiumBadge
import com.ideiassertiva.FypMatch.ui.components.PremiumTier
import com.ideiassertiva.FypMatch.ui.components.SectionHeader
import com.ideiassertiva.FypMatch.ui.components.UserAvatar
import com.ideiassertiva.FypMatch.ui.theme.MatchPink40

/**
 * Tela de configurações do FypMatch.
 * Exibe informações da conta, preferências de notificações, privacidade,
 * links de suporte e ações destrutivas (logout / exclusão de conta).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToPremium: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    // ─── Estado das preferências (SharedPreferences via remember + side-effect) ──
    val prefs = remember {
        context.getSharedPreferences("fypmatch_prefs", android.content.Context.MODE_PRIVATE)
    }

    var pushNotificacoes by remember { mutableStateOf(prefs.getBoolean("push_enabled", true)) }
    var notificarMatches by remember { mutableStateOf(prefs.getBoolean("notificar_matches", true)) }
    var notificarMensagens by remember { mutableStateOf(prefs.getBoolean("notificar_mensagens", true)) }
    var mostrarOnline by remember { mutableStateOf(prefs.getBoolean("mostrar_online", true)) }
    var mostrarDistancia by remember { mutableStateOf(prefs.getBoolean("mostrar_distancia", true)) }

    // ─── Diálogos de confirmação ─────────────────────────────────────────────
    var mostrarDialogLogout by remember { mutableStateOf(false) }
    var mostrarDialogExcluir by remember { mutableStateOf(false) }

    // Helper para persistir preferências
    fun salvarPreferencia(chave: String, valor: Boolean) {
        prefs.edit().putBoolean(chave, valor).apply()
    }

    // ─── Diálogo de logout ───────────────────────────────────────────────────
    if (mostrarDialogLogout) {
        AlertDialog(
            onDismissRequest = { mostrarDialogLogout = false },
            title = { Text("Sair da conta") },
            text = { Text("Tem certeza que deseja sair da sua conta?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        mostrarDialogLogout = false
                    }
                ) {
                    Text("Sair", color = Color(0xFFFF6600))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogLogout = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ─── Diálogo de excluir conta ────────────────────────────────────────────
    if (mostrarDialogExcluir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogExcluir = false },
            title = { Text("Excluir conta") },
            text = {
                Text(
                    "Esta ação é irreversível. Todos os seus dados, matches e conversas " +
                    "serão removidos permanentemente."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: Implementar exclusão completa (Firestore + Auth)
                        currentUser?.delete()
                        mostrarDialogExcluir = false
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogExcluir = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            // ═══════════════════════════════════════════════════════════════
            // SEÇÃO: CONTA
            // ═══════════════════════════════════════════════════════════════
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "Conta")
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Perfil do usuário
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            text = currentUser?.displayName ?: "Usuário",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    supportingContent = {
                        Text(
                            text = currentUser?.email ?: "",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = {
                        UserAvatar(
                            url = currentUser?.photoUrl?.toString(),
                            size = 48.dp
                        )
                    },
                    trailingContent = {
                        // Exibe badge premium se usuário tiver email verificado (placeholder)
                        if (currentUser?.isEmailVerified == true) {
                            PremiumBadge(tier = PremiumTier.PREMIUM)
                        }
                    }
                )
            }

            // Ver planos Premium
            item {
                ListItem(
                    headlineContent = { Text("Ver planos Premium", color = MatchPink40) },
                    leadingContent = {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MatchPink40
                        )
                    },
                    trailingContent = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    },
                    modifier = androidx.compose.ui.Modifier
                        .then(
                            Modifier
                        )
                )
                // Clique em toda a linha
                Surface(
                    onClick = onNavigateToPremium,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent
                ) {}
            }

            // Linha de clique funcional para "Ver planos Premium"
            item {
                Card(
                    onClick = onNavigateToPremium,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MatchPink40)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Ver planos Premium",
                            color = MatchPink40,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MatchPink40)
                    }
                }
            }

            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            // ═══════════════════════════════════════════════════════════════
            // SEÇÃO: NOTIFICAÇÕES
            // ═══════════════════════════════════════════════════════════════
            item { SectionHeader(title = "Notificações") }

            item {
                ListItem(
                    headlineContent = { Text("Push notifications") },
                    leadingContent = {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                    },
                    trailingContent = {
                        Switch(
                            checked = pushNotificacoes,
                            onCheckedChange = {
                                pushNotificacoes = it
                                salvarPreferencia("push_enabled", it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MatchPink40)
                        )
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Notificar novos matches") },
                    leadingContent = {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = MatchPink40)
                    },
                    trailingContent = {
                        Switch(
                            checked = notificarMatches,
                            onCheckedChange = {
                                notificarMatches = it
                                salvarPreferencia("notificar_matches", it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MatchPink40)
                        )
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Notificar mensagens") },
                    leadingContent = {
                        Icon(Icons.Default.Message, contentDescription = null)
                    },
                    trailingContent = {
                        Switch(
                            checked = notificarMensagens,
                            onCheckedChange = {
                                notificarMensagens = it
                                salvarPreferencia("notificar_mensagens", it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MatchPink40)
                        )
                    }
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            // ═══════════════════════════════════════════════════════════════
            // SEÇÃO: PRIVACIDADE
            // ═══════════════════════════════════════════════════════════════
            item { SectionHeader(title = "Privacidade") }

            item {
                ListItem(
                    headlineContent = { Text("Mostrar status online") },
                    leadingContent = {
                        Icon(Icons.Default.Circle, contentDescription = null, tint = Color.Green)
                    },
                    trailingContent = {
                        Switch(
                            checked = mostrarOnline,
                            onCheckedChange = {
                                mostrarOnline = it
                                salvarPreferencia("mostrar_online", it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MatchPink40)
                        )
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Mostrar distância no perfil") },
                    leadingContent = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    },
                    trailingContent = {
                        Switch(
                            checked = mostrarDistancia,
                            onCheckedChange = {
                                mostrarDistancia = it
                                salvarPreferencia("mostrar_distancia", it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MatchPink40)
                        )
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Bloquear / Denunciar usuários") },
                    leadingContent = {
                        Icon(Icons.Default.Block, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            // ═══════════════════════════════════════════════════════════════
            // SEÇÃO: SUPORTE
            // ═══════════════════════════════════════════════════════════════
            item { SectionHeader(title = "Suporte") }

            item {
                ListItem(
                    headlineContent = { Text("Central de ajuda") },
                    leadingContent = {
                        Icon(Icons.Default.Help, contentDescription = null, tint = MatchPink40)
                    },
                    trailingContent = {
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                    },
                    modifier = Modifier
                )
                // Abre o link da Central de ajuda
                Card(
                    onClick = { uriHandler.openUri("https://fypmatch.com/help") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Help, contentDescription = null, tint = MatchPink40)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Central de ajuda", modifier = Modifier.weight(1f))
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                    }
                }
            }

            item {
                Card(
                    onClick = { uriHandler.openUri("https://fypmatch.com/privacy") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PrivacyTip, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Política de Privacidade", modifier = Modifier.weight(1f))
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                    }
                }
            }

            item {
                Card(
                    onClick = { uriHandler.openUri("https://fypmatch.com/terms") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Termos de Uso", modifier = Modifier.weight(1f))
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                    }
                }
            }

            item {
                ListItem(
                    headlineContent = { Text("Reportar um problema") },
                    leadingContent = {
                        Icon(Icons.Default.BugReport, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            // ═══════════════════════════════════════════════════════════════
            // SEÇÃO: AÇÕES DE CONTA
            // ═══════════════════════════════════════════════════════════════
            item { SectionHeader(title = "Ações") }

            // Botão sair
            item {
                Button(
                    onClick = { mostrarDialogLogout = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6600)
                    )
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sair da conta")
                }
            }

            // Botão excluir conta
            item {
                Button(
                    onClick = { mostrarDialogExcluir = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Excluir conta")
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
