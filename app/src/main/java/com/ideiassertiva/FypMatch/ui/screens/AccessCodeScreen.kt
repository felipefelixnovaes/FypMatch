package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ideiassertiva.FypMatch.model.AccessCodeType
import com.ideiassertiva.FypMatch.ui.viewmodel.AccessCodeViewModel
import com.ideiassertiva.FypMatch.ui.viewmodel.RedeemState
import com.ideiassertiva.FypMatch.ui.viewmodel.getCodeTypeInfoList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessCodeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AccessCodeViewModel = viewModel()
) {
    val codeInput by viewModel.codeInput.collectAsState()
    val redeemState by viewModel.redeemState.collectAsState()
    val canUseCode by viewModel.canUseCode.collectAsState()
    
    when (val state = redeemState) {
        is RedeemState.Success -> {
            SuccessDialog(
                result = state.result,
                onDismiss = {
                    viewModel.clearResult()
                    onNavigateToHome()
                }
            )
        }
        else -> {}
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Código de Acesso") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
            }
        )
        
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedTextField(
                value = codeInput,
                onValueChange = viewModel::updateCodeInput,
                label = { Text("Código de Acesso") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = viewModel::redeemCode,
                modifier = Modifier.fillMaxWidth(),
                enabled = redeemState !is RedeemState.Loading
            ) {
                Text("Resgatar Código")
            }
        }
    }
}

@Composable
private fun CodeTypeCard(codeTypeInfo: com.ideiassertiva.FypMatch.ui.viewmodel.CodeTypeInfo) {
    val accentColor = when (codeTypeInfo.type) {
        AccessCodeType.BASIC -> Color(0xFF4CAF50)
        AccessCodeType.PREMIUM -> Color(0xFF2196F3)
        AccessCodeType.VIP -> Color(0xFFFF9800)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    codeTypeInfo.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                codeTypeInfo.duration?.let { duration ->
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("($duration)", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                codeTypeInfo.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            codeTypeInfo.benefits.forEach { benefit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(benefit, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SuccessDialog(
    result: com.ideiassertiva.FypMatch.model.AccessCodeResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                "Código Aplicado!",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                Text(
                    result.message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "Você será redirecionado para o FypMatch. Aproveite todas as funcionalidades liberadas!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                result.expiresAt?.let { expiresAt ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Válido até: ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(expiresAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ir para o App")
            }
        }
    )
}
