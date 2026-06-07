package com.ideiassertiva.FypMatch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ideiassertiva.FypMatch.model.ConnectionStatus

@Composable
fun ConnectionStatusHeader(
    status: ConnectionStatus?,
    onClick: () -> Unit
) {
    if (status == null) return

    val (icon, label, bgColor, textColor) = when (status) {
        ConnectionStatus.ICE_COLD -> arrayOf("🧊", "Esfriando", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        ConnectionStatus.COOLING_DOWN -> arrayOf("❄️", "Distante", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        ConnectionStatus.WARMING_UP -> arrayOf("🌱", "Se conhecendo", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        ConnectionStatus.ACTIVE -> arrayOf("✨", "Ativa", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        ConnectionStatus.ON_FIRE -> arrayOf("🔥", "Conexão Viva", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
    }

    Row(
        modifier = Modifier
            .padding(top = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor as androidx.compose.ui.graphics.Color)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon as String,
            fontSize = MaterialTheme.typography.labelSmall.fontSize
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label as String,
            fontSize = MaterialTheme.typography.labelSmall.fontSize,
            color = textColor as androidx.compose.ui.graphics.Color
        )
    }
}
