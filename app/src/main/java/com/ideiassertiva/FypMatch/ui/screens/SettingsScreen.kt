package com.ideiassertiva.FypMatch.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import com.ideiassertiva.FypMatch.domain.QuestionnaireCopy
import com.ideiassertiva.FypMatch.ui.components.PremiumBadge
import com.ideiassertiva.FypMatch.ui.components.PremiumTier
import com.ideiassertiva.FypMatch.ui.components.SectionHeader
import com.ideiassertiva.FypMatch.ui.components.UserAvatar
import com.ideiassertiva.FypMatch.ui.theme.FypColors
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ideiassertiva.FypMatch.ui.theme.AppThemeMode
import com.ideiassertiva.FypMatch.ui.viewmodel.ThemeViewModel
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
    onNavigateToStore: () -> Unit = {},
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToFilters: () -> Unit = {},
    onNavigateToSelfKnowledge: () -> Unit = {},
    onNavigateToConnectionMap: () -> Unit = {},
    onNavigateToQuickMode: () -> Unit = {},
    onNavigateToDeepMode: () -> Unit = {},
    onNavigateToAffiliate: () -> Unit = {},
    onNavigateToAds: () -> Unit = {},
    onNavigateToNeuroProfile: () -> Unit = {},
    onNavigateToComplementaryProfile: () -> Unit = {},
    onNavigateToPhotoVerification: () -> Unit = {},
    onNavigateToAvailability: () -> Unit = {},
    onNavigateToLifeValues: () -> Unit = {},
    onNavigateToAppGuide: () -> Unit = {},
    onNavigateToSafetyCenter: () -> Unit = {},
    onNavigateToProfileViewers: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()

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
                                onComplete = { message ->
                                    mostrarDialogExcluir = false
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
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
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            // ═══════════════════════════════════════════════════════════════
            // SEÇÃO: CONTA
            // ═══════════════════════════════════════════════════════════════
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "Conta", color = FypColors.ValuesAccent)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Card do perfil do usuário
            item {
                Card(
                    onClick = onNavigateToProfile,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Editar perfil e localização",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SettingsNavRow(
                    icon = Icons.Default.Tune,
                    title = "Filtros de descoberta",
                    subtitle = "Idade, distância, cidade, modo viagem e preferências",
                    tint = FypColors.Primary,
                    onClick = onNavigateToFilters
                )
            }

            item {
                SettingsNavRow(
                    icon = Icons.Default.VerifiedUser,
                    title = "Verificação de foto",
                    subtitle = "Selfie real com revisão manual para selo de confiança",
                    tint = FypColors.Success,
                    onClick = onNavigateToPhotoVerification
                )
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
            // SEÇÃO: SEU MAPA FYPMATCH
            // ═══════════════════════════════════════════════════════════════
            item { SectionHeader(title = QuestionnaireCopy.SETTINGS_MAP_SECTION, color = FypColors.ValuesAccent) }

            item {
                SettingsNavRow(
                    icon = Icons.Default.EventAvailable,
                    title = QuestionnaireCopy.AVAILABILITY_TITLE,
                    subtitle = QuestionnaireCopy.AVAILABILITY_SUBTITLE,
                    tint = FypColors.Success,
                    onClick = onNavigateToAvailability
                )
            }
            item {
                SettingsNavRow(
                    icon = Icons.Default.Psychology,
                    title = "Autoconhecimento",
                    subtitle = "Eneagrama, Linguagens do Cuidado e Arquétipos",
                    tint = FypColors.Secondary,
                    onClick = onNavigateToSelfKnowledge
                )
            }
            item {
                SettingsNavRow(
                    icon = Icons.Default.Visibility,
                    title = "Quem viu meu perfil",
                    subtitle = "Veja quem visitou seu perfil recentemente",
                    tint = FypColors.Primary,
                    onClick = onNavigateToProfileViewers
                )
            }
            item {
                SettingsNavRow(
                    icon = Icons.Default.Star,
                    title = QuestionnaireCopy.LIFE_VALUES_HUB_TITLE,
                    subtitle = QuestionnaireCopy.LIFE_VALUES_HUB_SUBTITLE,
                    tint = FypColors.ValuesAccent,
                    onClick = onNavigateToLifeValues
                )
            }
            item {
                SettingsNavRow(
                    icon = Icons.Default.Insights,
                    title = "Seu Mapa de Conexão",
                    subtitle = "Eixos relacionais gerados pelos seus questionários",
                    tint = FypColors.Primary,
                    onClick = onNavigateToConnectionMap
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
                    title = "Comunicação e bem-estar",
                    subtitle = "Preferências de comunicação e neurodiversidade",
                    tint = FypColors.Secondary,
                    onClick = onNavigateToNeuroProfile
                )
            }
            item {
                SettingsNavRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "Perfil complementar IA",
                    subtitle = "Importe contexto da sua IA pessoal para compatibilidade",
                    tint = FypColors.Secondary,
                    onClick = onNavigateToComplementaryProfile
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            // ═══════════════════════════════════════════════════════════════
            // SEÇÃO: CRÉDITOS & AFILIADOS
            // ═══════════════════════════════════════════════════════════════
            item { SectionHeader(title = "Créditos & Afiliados", color = FypColors.ValuesAccent) }

            item {
                SettingsNavRow(
                    icon = Icons.Default.ShoppingCart,
                    title = "Loja",
                    subtitle = "Créditos IA, supercurtidas e impulsionamento",
                    tint = FypColors.Primary,
                    onClick = onNavigateToStore
                )
            }

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
            // SEÇÃO: APARÊNCIA
            // ═══════════════════════════════════════════════════════════════
            item { SectionHeader(title = "Aparência", color = FypColors.ValuesAccent) }

            item {
                ThemeModeSelector(
                    selected = themeMode,
                    onSelect = themeViewModel::setThemeMode,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

            // ═══════════════════════════════════════════════════════════════
            // SEÇÃO: NOTIFICAÇÕES
            // ═══════════════════════════════════════════════════════════════
            item { SectionHeader(title = "Notificações", color = FypColors.ValuesAccent) }

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
            item { SectionHeader(title = "Privacidade", color = FypColors.ValuesAccent) }

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
                SettingsNavRow(
                    icon = Icons.Default.Shield,
                    title = "Central de segurança",
                    subtitle = "Bloqueios, denúncias, regras da comunidade e encontro seguro",
                    tint = FypColors.Primary,
                    onClick = onNavigateToSafetyCenter
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
            item { SectionHeader(title = "Suporte", color = FypColors.ValuesAccent) }

            item {
                SettingsNavRow(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = "Guia do app",
                    subtitle = "Veja prints guiados, balões explicativos e o mapa de recursos",
                    tint = FypColors.Primary,
                    onClick = onNavigateToAppGuide
                )
            }

            item {
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
            item { SectionHeader(title = "Ações", color = FypColors.ValuesAccent) }

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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeModeSelector(
    selected: AppThemeMode,
    onSelect: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tema do app",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Escolha entre o visual claro e o modo noturno do app",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                AppThemeMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = selected == mode,
                        onClick = { onSelect(mode) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = AppThemeMode.entries.size
                        ),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(mode.label)
                    }
                }
            }
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
