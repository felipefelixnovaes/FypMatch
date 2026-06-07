package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ideiassertiva.FypMatch.model.CompatibilityWeights
import com.ideiassertiva.FypMatch.model.ComplementaryProfile
import com.ideiassertiva.FypMatch.model.total
import com.ideiassertiva.FypMatch.ui.theme.FypColors
import com.ideiassertiva.FypMatch.ui.viewmodel.ComplementaryProfileUiState
import com.ideiassertiva.FypMatch.ui.viewmodel.ComplementaryProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplementaryProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ComplementaryProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    ComplementaryProfileContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onImportedTextChange = viewModel::updateImportedText,
        onSave = viewModel::saveImportedText,
        onClear = viewModel::clearProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComplementaryProfileContent(
    uiState: ComplementaryProfileUiState,
    onNavigateBack: () -> Unit,
    onImportedTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var copiedPrompt by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Apagar perfil complementar") },
            text = {
                Text("Isso remove o perfil importado do seu cadastro. Você poderá gerar e importar outro depois.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClear()
                    }
                ) {
                    Text("Apagar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil complementar IA", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
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
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeaderCard(profile = uiState.savedProfile, hasSavedProfile = uiState.hasSavedProfile)
            }

            item {
                PromptCard(
                    prompt = uiState.prompt,
                    copiedPrompt = copiedPrompt,
                    onCopyPrompt = {
                        clipboard.setText(AnnotatedString(uiState.prompt))
                        copiedPrompt = true
                    }
                )
            }

            item {
                ImportCard(
                    importedText = uiState.importedText,
                    isSaving = uiState.isSaving,
                    onImportedTextChange = onImportedTextChange,
                    onSave = onSave
                )
            }

            uiState.error?.let { error ->
                item {
                    StatusMessage(
                        iconTint = MaterialTheme.colorScheme.error,
                        text = error
                    )
                }
            }

            if (uiState.saveSuccess) {
                item {
                    StatusMessage(
                        iconTint = FypColors.Success,
                        text = "Perfil complementar salvo e pronto para apoiar compatibilidade e conselheira."
                    )
                }
            }

            if (uiState.hasSavedProfile) {
                item {
                    SavedProfilePreview(profile = uiState.savedProfile)
                }

                item {
                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apagar perfil complementar")
                    }
                }
            }

            item { PrivacyNote() }
        }
    }
}

@Composable
private fun HeaderCard(profile: ComplementaryProfile, hasSavedProfile: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(FypColors.Primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = FypColors.Primary)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasSavedProfile) "Perfil complementar ativo" else "Importe uma camada extra de contexto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (hasSavedProfile) {
                        "Confiança: ${profile.generalConfidence.ifBlank { "não informada" }}"
                    } else {
                        "Use sua IA pessoal para gerar dados complementares de compatibilidade."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (profile.updatedAt > 0L) {
                    Text(
                        text = "Atualizado em ${formatUpdatedAt(profile.updatedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (hasSavedProfile) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FypColors.Success)
            }
        }
    }
}

@Composable
private fun PromptCard(
    prompt: String,
    copiedPrompt: Boolean,
    onCopyPrompt: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("1. Copie o prompt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Cole na IA com quem você conversa com frequência. Ela deve usar apenas o histórico que já conhece.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = prompt,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp, max = 220.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                maxLines = 10
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onCopyPrompt,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = FypColors.Primary)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (copiedPrompt) "Prompt copiado" else "Copiar prompt")
            }
        }
    }
}

@Composable
private fun ImportCard(
    importedText: String,
    isSaving: Boolean,
    onImportedTextChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("2. Cole a resposta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "O FypMatch importa o JSON do item 12 e guarda o texto bruto para revisão.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = importedText,
                onValueChange = onImportedTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 300.dp),
                placeholder = { Text("Cole aqui o Perfil Complementar FypMatch gerado pela sua IA...") },
                textStyle = MaterialTheme.typography.bodySmall,
                minLines = 8,
                maxLines = 14,
                supportingText = {
                    Text("${importedText.length} caracteres")
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = importedText.isNotBlank() && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = FypColors.Primary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isSaving) "Salvando..." else "Salvar perfil complementar")
            }
        }
    }
}

@Composable
private fun SavedProfilePreview(profile: ComplementaryProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resumo importado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = profile.summary.ifBlank { "Resumo não informado." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PreviewLine("Valores centrais", profile.coreValues.take(6))
            PreviewLine("Tags do algoritmo", profile.algorithmicTags.take(8))
            PreviewLine("Sinais verdes", profile.greenFlags.take(4))
            WeightsLine(profile.weights)
        }
    }
}

@Composable
private fun PreviewLine(title: String, values: List<String>) {
    if (values.isEmpty()) return
    Spacer(modifier = Modifier.height(12.dp))
    Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    Text(
        text = values.joinToString(", "),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun WeightsLine(weights: CompatibilityWeights) {
    val total = weights.total()
    if (total == 0) return
    Spacer(modifier = Modifier.height(12.dp))
    Text("Pesos sugeridos", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    Text(
        text = "Soma $total/100: valores ${weights.values}, comunicação ${weights.communicationStyle}, maturidade ${weights.emotionalMaturity}, estilo afetivo ${weights.affectionStyle}.",
        style = MaterialTheme.typography.bodySmall,
        color = if (total == 100) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
    )
}

@Composable
private fun StatusMessage(iconTint: Color, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = iconTint.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = iconTint)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PrivacyNote() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Privacidade", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(
                "Dados sensíveis devem ser opcionais, protegidos e nunca usados automaticamente. O perfil complementar apoia compatibilidade, mas não substitui questionários nem julgamento humano.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatUpdatedAt(updatedAt: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(Date(updatedAt))
}
