package com.ideiassertiva.FypMatch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ideiassertiva.FypMatch.ui.theme.MatchPink40
import com.ideiassertiva.FypMatch.ui.theme.MatchPurple40

// ─────────────────────────────────────────────────────────────────────────────
// GRADIENTE PADRÃO DO FYPMATCH
// ─────────────────────────────────────────────────────────────────────────────

/** Gradiente horizontal rosa→roxo, identidade visual do FypMatch */
val FypGradient: Brush = Brush.horizontalGradient(
    colors = listOf(MatchPink40, MatchPurple40)
)

// ─────────────────────────────────────────────────────────────────────────────
// FYP GRADIENT BUTTON
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Botão primário do FypMatch com fundo em gradiente rosa→roxo.
 * Exibe [CircularProgressIndicator] quando [isLoading] for verdadeiro.
 */
@Composable
fun FypGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val backgroundModifier = if (enabled && !isLoading) {
        Modifier.background(FypGradient, shape = RoundedCornerShape(16.dp))
    } else {
        Modifier.background(
            Color(0xFFE91E63).copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .then(backgroundModifier)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        // Captura o clique na Box toda
        Surface(
            onClick = { if (enabled && !isLoading) onClick() },
            modifier = Modifier.matchParentSize(),
            color = Color.Transparent
        ) {}

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FYP OUTLINE BUTTON
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Botão secundário do FypMatch com borda na cor [MatchPink40].
 */
@Composable
fun FypOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MatchPink40
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MatchPink40)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREMIUM BADGE
// ─────────────────────────────────────────────────────────────────────────────

/** Tier de assinatura premium */
enum class PremiumTier { PREMIUM, VIP }

/**
 * Badge pequena indicando o nível premium do usuário.
 * Exibe gradiente rosa→roxo para PREMIUM e dourado para VIP.
 */
@Composable
fun PremiumBadge(
    tier: PremiumTier,
    modifier: Modifier = Modifier
) {
    val gradiente = when (tier) {
        PremiumTier.VIP -> Brush.horizontalGradient(
            listOf(Color(0xFFFFD700), Color(0xFFFF8C00))
        )
        PremiumTier.PREMIUM -> FypGradient
    }

    val icone = if (tier == PremiumTier.VIP) "👑" else "✨"
    val rotulo = if (tier == PremiumTier.VIP) "VIP" else "Premium"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(gradiente)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = icone,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
            Text(
                text = rotulo,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// USER AVATAR
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Avatar circular do usuário.
 * Carrega imagem via Coil. Se [url] for nulo/vazio, exibe ícone de pessoa
 * com fundo em gradiente FypMatch.
 */
@Composable
fun UserAvatar(
    url: String?,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = "Foto do usuário",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            // Placeholder com gradiente FypMatch
            Box(
                modifier = Modifier
                    .size(size)
                    .background(FypGradient, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Sem foto",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(size * 0.5f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FYP TEXT FIELD
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Campo de texto estilizado do FypMatch.
 * Suporta máscara de senha com botão de toggle de visibilidade.
 * Exibe [errorMessage] em vermelho abaixo do campo quando fornecido.
 */
@Composable
fun FypTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    errorMessage: String? = null
) {
    var senhaVisivel by remember { mutableStateOf(false) }

    val visualTransformation = when {
        isPassword && !senhaVisivel -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            isError = errorMessage != null,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                        Icon(
                            imageVector = if (senhaVisivel) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = if (senhaVisivel) "Ocultar senha"
                            else "Mostrar senha"
                        )
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MatchPink40,
                focusedLabelColor = MatchPink40,
                cursorColor = MatchPink40
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Mensagem de erro inline
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPATIBILITY BADGE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Badge de compatibilidade exibindo percentual de afinidade.
 * - Verde (>80%): boa compatibilidade
 * - Amarelo (>60%): compatibilidade média
 * - Cinza (<60%): baixa compatibilidade
 */
@Composable
fun CompatibilityBadge(
    score: Double,
    modifier: Modifier = Modifier
) {
    val corFundo = when {
        score > 80 -> Color(0xFF4CAF50)  // Verde
        score > 60 -> Color(0xFFFF9800)  // Amarelo/Laranja
        else -> Color(0xFF9E9E9E)         // Cinza
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(50)),
        color = corFundo.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${score.toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = corFundo
            )
            Text(
                text = "❤️",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION HEADER
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Cabeçalho de seção com estilo [MaterialTheme.typography.titleSmall] + [HorizontalDivider].
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// USER INFO CARD
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Card compacto com avatar, nome, idade e cidade do usuário.
 */
@Composable
fun UserInfoCard(
    name: String,
    age: Int,
    city: String,
    photoUrl: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UserAvatar(url = photoUrl, size = 52.dp)

            Column {
                Text(
                    text = "$name, $age",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = city,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
