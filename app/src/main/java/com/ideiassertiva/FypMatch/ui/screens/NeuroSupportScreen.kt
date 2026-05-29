package com.ideiassertiva.FypMatch.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ideiassertiva.FypMatch.ui.components.FypGradientButton
import com.ideiassertiva.FypMatch.ui.theme.MatchPink40
import com.ideiassertiva.FypMatch.ui.theme.MatchPurple40

// ─── Dados internos ───────────────────────────────────────────────────────────

private data class PistaSocial(
    val emoji: String,
    val titulo: String,
    val explicacao: String,
    val exemplos: List<String>,
    val comoResponder: String
)

private val pistasSociais = listOf(
    PistaSocial(
        emoji = "😅",
        titulo = "Sorriso nervoso",
        explicacao = "Pode indicar constrangimento leve ou humor autodepreciativo.",
        exemplos = listOf("Acabei de tropeçar 😅", "Não sou muito bom nisso 😅"),
        comoResponder = "Seja encorajador: \"Que nada, acontece com todo mundo!\" ou \"Você se sai bem sim.\""
    ),
    PistaSocial(
        emoji = "💬",
        titulo = "Respostas curtas",
        explicacao = "Pode indicar pressa, cansaço ou desconforto no momento.",
        exemplos = listOf("\"ok\"", "\"ah\"", "\"sim\""),
        comoResponder = "Mude de assunto com leveza ou pergunte se é um bom momento para conversar."
    ),
    PistaSocial(
        emoji = "…",
        titulo = "Reticências...",
        explicacao = "Pode indicar hesitação ou que há mais a dizer.",
        exemplos = listOf("\"Eu queria falar uma coisa...\"", "\"É que...\""),
        comoResponder = "Dê espaço: \"Pode falar, estou ouvindo.\" — sem pressionar."
    ),
    PistaSocial(
        emoji = "😂",
        titulo = "kkk / haha",
        explicacao = "Indica que a pessoa achou engraçado ou está aliviando tensão.",
        exemplos = listOf("\"Que história engraçada haha\"", "\"Verdade kkk\""),
        comoResponder = "\"Também achei graça 😄\" ou continue com o mesmo tom leve."
    )
)

private val afirmacoes = listOf(
    "Eu mereço conexões que me respeitam.",
    "Minha forma de sentir é válida e única.",
    "É OK precisar de tempo para processar.",
    "Cada passo é progresso, por menor que seja.",
    "Sou capaz de criar vínculos genuínos."
)

// ─── Tela principal ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuroSupportScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNeuroProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suporte Neuro", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Seção 1: Assistente de Mensagem ──────────────────────────────
            item { SecaoAssistenteMensagem() }

            // ── Seção 2: Decodificador de Pistas Sociais ─────────────────────
            item { SecaoPistasSociais() }

            // ── Seção 3: Suporte Emocional ────────────────────────────────────
            item { SecaoSuporteEmocional() }

            // ── Seção 4: Configurações rápidas ───────────────────────────────
            item {
                SecaoConfiguracoes(
                    context = context,
                    onNavigateToNeuroProfile = onNavigateToNeuroProfile
                )
            }
        }
    }
}

// ─── Seção 1 — Assistente de Mensagem ────────────────────────────────────────

@Composable
private fun SecaoAssistenteMensagem() {
    var mensagem by remember { mutableStateOf("") }
    var analisado by remember { mutableStateOf(false) }
    var expandido by remember { mutableStateOf(true) }

    // Resultado da análise (lógica local)
    val resultado = remember(mensagem) {
        if (mensagem.isBlank()) null
        else analisarMensagem(mensagem)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header expansível
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("💬", fontSize = 24.sp)
                    Text(
                        text = "Assistente de Mensagem",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Icon(
                    imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expandido) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    OutlinedTextField(
                        value = mensagem,
                        onValueChange = { mensagem = it; analisado = false },
                        placeholder = { Text("Digite sua mensagem para analisar...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MatchPink40,
                            cursorColor = MatchPink40
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    FypGradientButton(
                        text = "Analisar",
                        onClick = { if (mensagem.isNotBlank()) analisado = true },
                        enabled = mensagem.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Resultado da análise
                    AnimatedVisibility(
                        visible = analisado && resultado != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        resultado?.let { r ->
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                // Tom detectado
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MatchPink40.copy(alpha = 0.07f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Tom: ${r.tom}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MatchPink40
                                        )
                                        Text(r.tonEmoji, fontSize = 18.sp)
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                // Clareza
                                Text(
                                    text = "Clareza da mensagem",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { r.clareza },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MatchPurple40,
                                    trackColor = MatchPurple40.copy(alpha = 0.2f)
                                )
                                Text(
                                    text = "${(r.clareza * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Sugestões
                                if (r.sugestoes.isNotEmpty()) {
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = "Sugestões",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    r.sugestoes.forEach { sug ->
                                        Row(
                                            modifier = Modifier.padding(top = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text("•", color = MatchPink40, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = sug,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Análise de mensagem local (sem backend)
private data class ResultadoAnalise(
    val tom: String,
    val tonEmoji: String,
    val clareza: Float,
    val sugestoes: List<String>
)

private fun analisarMensagem(texto: String): ResultadoAnalise {
    val lower = texto.lowercase()
    val sugestoes = mutableListOf<String>()

    // Tom
    val (tom, tonEmoji) = when {
        lower.contains("!") && (lower.contains("😊") || lower.contains(":)")) -> "Animado" to "😊"
        lower.contains("...") -> "Pensativo" to "🤔"
        lower.contains("desculpa") || lower.contains("sorry") -> "Apologético" to "🙏"
        lower.contains("😢") || lower.contains("triste") -> "Triste" to "😢"
        else -> "Neutro" to "✓"
    }

    // Clareza
    var clareza = 1.0f
    if (texto.length > 200) {
        clareza -= 0.2f
        sugestoes.add("Considere dividir a mensagem em partes menores.")
    }
    val vagos = listOf("tipo", "sei lá", "meio que", "coisa", "negócio")
    if (vagos.any { lower.contains(it) }) {
        clareza -= 0.2f
        sugestoes.add("Substitua palavras vagas por termos mais específicos.")
    }
    if (!texto.contains("?") && texto.length > 30) {
        sugestoes.add("Adicionar uma pergunta mantém a conversa fluindo.")
    }

    return ResultadoAnalise(
        tom = tom,
        tonEmoji = tonEmoji,
        clareza = clareza.coerceIn(0.2f, 1.0f),
        sugestoes = sugestoes.take(3)
    )
}

// ─── Seção 2 — Decodificador de Pistas Sociais ───────────────────────────────

@Composable
private fun SecaoPistasSociais() {
    var expandida by remember { mutableStateOf(false) }
    val expandidas = remember { mutableStateMapOf<Int, Boolean>() }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandida = !expandida },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🔍", fontSize = 24.sp)
                    Text(
                        text = "Entender Sinais Sociais",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Icon(
                    imageVector = if (expandida) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expandida) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    pistasSociais.forEachIndexed { index, pista ->
                        val itemExpandido = expandidas[index] == true
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { expandidas[index] = !itemExpandido },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (itemExpandido)
                                    MatchPurple40.copy(alpha = 0.07f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(pista.emoji, fontSize = 20.sp)
                                    Text(
                                        text = pista.titulo,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (itemExpandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = itemExpandido,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Column(modifier = Modifier.padding(top = 10.dp)) {
                                        Text(
                                            text = pista.explicacao,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = "Exemplos:",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        pista.exemplos.forEach { ex ->
                                            Text(
                                                text = "• $ex",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Card(
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = MatchPurple40.copy(alpha = 0.1f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text("💡", fontSize = 14.sp)
                                                Text(
                                                    text = pista.comoResponder,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MatchPurple40,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Seção 3 — Suporte Emocional ─────────────────────────────────────────────

@Composable
private fun SecaoSuporteEmocional() {
    data class CardEmocional(
        val emoji: String,
        val titulo: String,
        val cor: Color,
        val conteudo: @Composable () -> Unit
    )

    val passos54321 = listOf(
        "5 coisas que você pode VER ao redor",
        "4 coisas que você pode TOCAR",
        "3 coisas que você pode OUVIR",
        "2 coisas que você pode CHEIRAR",
        "1 coisa que você pode SABOREAR"
    )

    val cards = listOf(
        CardEmocional(
            emoji = "💙",
            titulo = "Técnica 5-4-3-2-1",
            cor = Color(0xFF2196F3)
        ) {
            Column {
                passos54321.forEachIndexed { i, texto ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .then(
                                    Modifier.background(
                                        Color(0xFF2196F3).copy(alpha = 0.15f),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${i + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3)
                                )
                            )
                        }
                        Text(
                            text = texto,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        CardEmocional(
            emoji = "🌬️",
            titulo = "Respiração guiada",
            cor = Color(0xFF4CAF50)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "🫁 Inspire..." to "4 segundos",
                    "⏸ Segure..." to "4 segundos",
                    "💨 Expire..." to "4 segundos",
                    "⏸ Segure..." to "4 segundos"
                ).forEach { (acao, tempo) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = acao, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = tempo,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4CAF50)
                            )
                        )
                    }
                    if (acao != "⏸ Segure...") HorizontalDivider(color = Color(0xFF4CAF50).copy(alpha = 0.15f))
                }
                Text(
                    text = "Repita 4 vezes para sentir o efeito.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        CardEmocional(
            emoji = "💚",
            titulo = "Afirmações positivas",
            cor = Color(0xFF66BB6A)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                afirmacoes.forEach { afirmacao ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("✦", color = Color(0xFF66BB6A), fontSize = 12.sp)
                        Text(
                            text = afirmacao,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Suporte Emocional",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 4.dp)
        )

        cards.forEach { card ->
            var expandido by remember { mutableStateOf(false) }
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandido = !expandido },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(card.emoji, fontSize = 22.sp)
                            Text(
                                text = card.titulo,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Icon(
                            imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = card.cor
                        )
                    }

                    AnimatedVisibility(
                        visible = expandido,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            card.conteudo()
                        }
                    }
                }
            }
        }
    }
}

// ─── Seção 4 — Configurações rápidas ─────────────────────────────────────────

@Composable
private fun SecaoConfiguracoes(
    context: Context,
    onNavigateToNeuroProfile: () -> Unit
) {
    val prefs = remember { context.getSharedPreferences("neuro_settings", Context.MODE_PRIVATE) }
    var reduzirAnimacoes by remember {
        mutableStateOf(prefs.getBoolean("reduce_animations", false))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚙️  Configurações rápidas",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(Modifier.height(16.dp))

            // Switch reduzir animações
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reduzir animações",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Menos movimento na interface",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = reduzirAnimacoes,
                    onCheckedChange = { novo ->
                        reduzirAnimacoes = novo
                        prefs.edit().putBoolean("reduce_animations", novo).apply()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MatchPink40
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            TextButton(
                onClick = onNavigateToNeuroProfile,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Editar perfil neuro completo →",
                    color = MatchPink40,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
