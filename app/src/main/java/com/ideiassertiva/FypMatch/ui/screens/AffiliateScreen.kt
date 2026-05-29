package com.ideiassertiva.FypMatch.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ideiassertiva.FypMatch.model.Referral
import com.ideiassertiva.FypMatch.model.ReferralStatus
import com.ideiassertiva.FypMatch.ui.components.FypGradientButton
import com.ideiassertiva.FypMatch.ui.components.FypTextField
import com.ideiassertiva.FypMatch.ui.theme.MatchPink40
import com.ideiassertiva.FypMatch.ui.theme.MatchPurple40
import com.ideiassertiva.FypMatch.viewmodel.AffiliateViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AffiliateScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AffiliateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentAffiliate by viewModel.currentAffiliate.collectAsStateWithLifecycle()
    val referrals by viewModel.referrals.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Mostrar snackbar para mensagens de sucesso/erro
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearMessages()
        }
    }

    // Estado do dialogo de cadastro
    var showRegisterDialog by remember { mutableStateOf(false) }
    var registerName by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var registerPhone by remember { mutableStateOf("") }

    // Estado do campo de saque
    var payoutValue by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Programa de Afiliados") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->

        if (currentAffiliate == null) {
            // ── Estado: Nao e afiliado ainda ──────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🤝",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ganhe com o FypMatch!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Indique amigos e ganhe 20% nas assinaturas Premium e 25% nas VIP",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        BenefitItem("Ganhe comissao em cada indicacao confirmada")
                        Spacer(modifier = Modifier.height(8.dp))
                        BenefitItem("Saque a partir de R\$ 50,00 via Pix")
                        Spacer(modifier = Modifier.height(8.dp))
                        BenefitItem("Painel completo com suas estatisticas")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                FypGradientButton(
                    text = "Quero ser afiliado!",
                    onClick = { showRegisterDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = uiState.isLoading
                )
            }
        } else {
            // ── Estado: Ja e afiliado — dashboard ─────────────────────────
            val affiliate = currentAffiliate!!
            val stats = affiliate.stats
            val referralLink = viewModel.generateReferralLink(affiliate.code)
            val brl = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // ── Grid de estatisticas ──────────────────────────────────
                item {
                    Text(
                        text = "Suas Estatísticas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            label = "Total\nIndicações",
                            value = "${stats.totalReferrals}",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Ganhos\nPendentes",
                            value = brl.format(stats.pendingEarnings),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            label = "Ganhos\nTotais",
                            value = brl.format(stats.totalEarnings),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Taxa\nConversão",
                            value = "${(stats.conversionRate * 100).toInt()}%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ── Card do codigo ────────────────────────────────────────
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Seu Código",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = affiliate.code,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MatchPink40
                                )
                                Row {
                                    IconButton(onClick = {
                                        copyToClipboard(context, "Código de afiliado", affiliate.code)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Código copiado!")
                                        }
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar código")
                                    }
                                    IconButton(onClick = {
                                        shareText(context, "Use meu código ${affiliate.code} no FypMatch e ganhe desconto! $referralLink")
                                    }) {
                                        Icon(Icons.Default.Share, contentDescription = "Compartilhar")
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Card do link ──────────────────────────────────────────
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Link de Indicação",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = referralLink,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MatchPurple40,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    copyToClipboard(context, "Link de indicação", referralLink)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Link copiado!")
                                    }
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar link")
                                }
                            }
                        }
                    }
                }

                // ── Ultimas indicacoes ────────────────────────────────────
                item {
                    Text(
                        text = "Últimas Indicações",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (referrals.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhuma indicação ainda. Compartilhe seu link!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(referrals.take(5)) { referral ->
                        ReferralItem(referral = referral)
                    }
                }

                // ── Card de saque ─────────────────────────────────────────
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Solicitar Saque",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Saldo disponível: ${brl.format(stats.pendingEarnings)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = payoutValue,
                                onValueChange = { payoutValue = it },
                                label = { Text("Valor (mínimo R\$ 50,00)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MatchPink40,
                                    focusedLabelColor = MatchPink40
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FypGradientButton(
                                text = "Solicitar Saque",
                                onClick = {
                                    val amount = payoutValue.replace(",", ".").toDoubleOrNull()
                                    if (amount == null || amount < 50.0) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Valor mínimo para saque é R\$ 50,00")
                                        }
                                    } else {
                                        viewModel.requestPayout(affiliate.id, amount)
                                        payoutValue = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                isLoading = uiState.isLoading
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    // ── Dialogo de cadastro de afiliado ───────────────────────────────────────
    if (showRegisterDialog) {
        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = { Text("Cadastro de Afiliado") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FypTextField(
                        value = registerName,
                        onValueChange = { registerName = it },
                        label = "Nome completo"
                    )
                    FypTextField(
                        value = registerEmail,
                        onValueChange = { registerEmail = it },
                        label = "E-mail",
                        keyboardType = KeyboardType.Email
                    )
                    FypTextField(
                        value = registerPhone,
                        onValueChange = { registerPhone = it },
                        label = "Telefone (opcional)",
                        keyboardType = KeyboardType.Phone
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (registerName.isBlank() || registerEmail.isBlank()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Nome e e-mail são obrigatórios")
                            }
                        } else {
                            viewModel.registerAffiliate(
                                userId = "current_user", // Em producao viria da autenticacao
                                name = registerName,
                                email = registerEmail,
                                phoneNumber = registerPhone
                            )
                            showRegisterDialog = false
                        }
                    }
                ) {
                    Text("Cadastrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ── Componentes auxiliares ────────────────────────────────────────────────────

@Composable
private fun BenefitItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("✅ ")
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MatchPink40
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReferralItem(referral: Referral) {
    val (badgeColor, badgeLabel) = when (referral.status) {
        ReferralStatus.PENDING -> Color(0xFFFFC107) to "Pendente"
        ReferralStatus.CONFIRMED -> Color(0xFF4CAF50) to "Confirmado"
        ReferralStatus.PAID -> Color(0xFF2196F3) to "Pago"
        ReferralStatus.CANCELLED -> Color(0xFF9E9E9E) to "Cancelado"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = referral.referredUserEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = referral.subscriptionType,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = badgeColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = badgeLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = badgeColor
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar via"))
}
