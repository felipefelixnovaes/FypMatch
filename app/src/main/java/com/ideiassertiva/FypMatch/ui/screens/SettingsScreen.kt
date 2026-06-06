package com.ideiassertiva.FypMatch.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.ideiassertiva.FypMatch.ui.components.PremiumBadge
import com.ideiassertiva.FypMatch.ui.components.PremiumTier
import com.ideiassertiva.FypMatch.ui.components.SectionHeader
import com.ideiassertiva.FypMatch.ui.components.UserAvatar
import com.ideiassertiva.FypMatch.ui.theme.FypColors
import com.ideiassertiva.FypMatch.ui.viewmodel.SettingsViewModel

/**
 * Tela de configurações do FypMatch.
 * Exibe informações da conta, preferências de notificações, privacidade,
 * links de suporte e ações destrutivas (logout / exclusão de conta).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToPremium: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSelfKnowledge: () -> Unit = {},
    onNavigateToQuickMode: () -> Unit = {},
    onNavigateToDeepMode: () -> Unit = {},
    onNavigateToAffiliate: () -> Unit = {},
    onNavigateToAds: () -> Unit = {},
    onNavigateToNeuroProfile: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

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
    var mostrarDialogDenuncia by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    // ─── Diálogo de bloquear/denunciar ───────────────────────────────────────
    if (mostrarDialogDenuncia) {
        AlertDialog(
            onDismissRequest = { mostrarDialogDenuncia = false },
            icon = { Icon(Icons.Default.Shield, contentDescription = null, tint = FypColors.Primary) },
            title = { Text("Segurança e denúncias") },
            text = {
                Text(
                    "Para bloquear ou denunciar uma pessoa específica, abra o perfil dela e toque no menu (⋯).\n\n" +
                    "Em casos urgentes ou de assédio, fale diretamente com nossa equipe de segurança."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogDenuncia = false
                    enviarEmailSuporte(
                        context = context,
                        destino = "seguranca@fypmatch.com",
                        assunto = "Denúncia de usuário — FypMatch"
                    )
                }) {
                    Text("Falar com segurança", color = FypColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogDenuncia = false }) {
                    Text("Fechar")
                }
            }
        )
    }

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
                        viewModel.signOut()
                        mostrarDialogLogout = false
                        onNavigateToLogin()
                    }
                ) {
                    Text("Sair", color = FypColors.Tertiary)
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
                Column {
                    Text(
                        "Esta ação é irreversível. Todos os seus dados, matches e conversas " +
                        "serão removidos permanentemente."
                    )
                    deleteError?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(err, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                if (uiState.isDeletingAccount) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    TextButton(
                        onClick = {
                            deleteError = null
                            viewModel.deleteAccount(
                                onComplete = {
                                    mostrarDialogExcluir = false
                                    onNavigateToLogin()
                                },
                                onError = { err ->
                                    deleteError = err
                                }
                            )
                        }
                    ) {
                        Text("Excluir", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogExcluir = false
                    deleteError = null
                }) {
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

            // Card do perfil do usuário
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(url = currentUser?.photoUrl?.toString(), size = 56.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.displayName ?: "Marina Alves",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ver seu perfil",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FypColors.Primary
                            )
                        }
                    }
                }
            }

            // Card Premium em gradiente de marca
            item {
                Card(
                    onClick = onNavigateToPremium,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FypColors.BrandGradientDiagonal)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Ver planos Premium",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Mais curtidas, visibilidade e recursos exclusivos",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // ═══════════════════════════════════════════════════════════════
            // SEÇÃO: DESCOBERTAS & AUTOCONHECIMENTO
            // ═══════════════════════════════════════════════════════════════
            item { SectionHeader(title = "Descobertas & Autoconhecimento") }

            item {
                SettingsNavRow(
                    icon = Icons.Default.Psychology,
                    title = "Autoconhecimento",
                    subtitle = "Eneagrama, Linguagens do Amor e Arquétipos",
                    tint = FypColors.Secondary,
                    onClick = onNavigateToSelfKnowledge
                )
            }
            item {
                SettingsNavRow(
                    icon = Icons.Default.Bolt,
                    title = "Questionário rápido",
                    subtitle = "Descubra sua compatibilidade em minutos",
                    tint = FypColors.Primary,
                    onClick = onNavigateToQuickMode
                )
            }
            item {
                SettingsNavRow(
                    icon = Icons.Default.Insights,
                    title = "Questionário profundo",
                    subtitle = "Análise detalhada de personalidade e valores",
                    tint = FypColors.Primary,
                    onClick = onNavigateToDeepMode
                )
            }
            item {
                SettingsNavRow(
                    icon = Icons.Default.Favorite,
                    title = "Perfil de neurodiversidade",
                    subtitle = "Personalize sua experiência",
                    tint = FypColors.Secondary,
                    onClick = onNavigateToNeuroProfile
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            // ═══════════════════════════════════════════════════════════════
            // SEÇÃO: CRÉDITOS & AFILIADOS
            // ═══════════════════════════════════════════════════════════════
            item { SectionHeader(title = "Créditos & Afiliados") }

            item {
                SettingsNavRow(
                    icon = Icons.Default.PlayCircle,
                    title = "Ganhar créditos IA",
                    subtitle = "Assista anúncios e ganhe créditos",
                    tint = FypColors.Primary,
                    onClick = onNavigateToAds
                )
            }
            item {
                SettingsNavRow(
                    icon = Icons.Default.Group,
                    title = "Programa de afiliados",
                    subtitle = "Indique amigos e ganhe comissões",
                    tint = FypColors.Secondary,
                    onClick = onNavigateToAffiliate
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            // ═══════════════════════════════════════════════════════════════
            // SEÇÃO: NOTIFICAÇÕES
            // ═══════════════════════════════════════════════════════════════
            item { SectionHeader(title = "Notificações") }

            item {
                ListItem(
                    headlineContent = { Text("Notificações push") },
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
                            colors = SwitchDefaults.colors(checkedThumbColor = FypColors.Primary)
                        )
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Notificar novos matches") },
                    leadingContent = {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = FypColors.Primary)
                    },
                    trailingContent = {
                        Switch(
                            checked = notificarMatches,
                            onCheckedChange = {
                                notificarMatches = it
                                salvarPreferencia("notificar_matches", it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = FypColors.Primary)
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
                            colors = SwitchDefaults.colors(checkedThumbColor = FypColors.Primary)
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
                        Icon(Icons.Default.Circle, contentDescription = null, tint = FypColors.Success)
                    },
                    trailingContent = {
                        Switch(
                            checked = mostrarOnline,
                            onCheckedChange = {
                                mostrarOnline = it
                                salvarPreferencia("mostrar_online", it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = FypColors.Primary)
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
                            colors = SwitchDefaults.colors(checkedThumbColor = FypColors.Primary)
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
                    },
                    modifier = Modifier.clickable { mostrarDialogDenuncia = true }
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
                        Icon(Icons.Default.Help, contentDescription = null, tint = FypColors.Primary)
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
                        Icon(Icons.Default.Help, contentDescription = null, tint = FypColors.Primary)
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
                    },
                    modifier = Modifier.clickable {
                        enviarEmailSuporte(
                            context = context,
                            destino = "suporte@fypmatch.com",
                            assunto = "Reportar problema no app FypMatch"
                        )
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
                        containerColor = FypColors.Tertiary
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

@Composable
private fun SettingsNavRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(tint.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Abre o app de e-mail do usuário com destinatário e assunto pré-preenchidos.
 * Se não houver app de e-mail, exibe um Toast com o endereço de contato.
 */
private fun enviarEmailSuporte(context: Context, destino: String, assunto: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$destino")
        putExtra(Intent.EXTRA_SUBJECT, assunto)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Enviar e-mail"))
    } catch (e: Exception) {
        Toast.makeText(context, "Envie um e-mail para $destino", Toast.LENGTH_LONG).show()
    }
}
