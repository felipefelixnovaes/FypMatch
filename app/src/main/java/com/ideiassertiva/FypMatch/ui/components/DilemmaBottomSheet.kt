package com.ideiassertiva.FypMatch.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class DilemmaMission(
    val title: String,
    val question: String,
    val options: List<String>
) {
    fun asChatMessage(): String {
        return buildString {
            appendLine("Missão de Conexão: $title")
            appendLine()
            appendLine(question)
            appendLine()
            options.forEachIndexed { index, option ->
                appendLine("${'A' + index}) $option")
            }
        }.trim()
    }
}

private val connectionMissions = listOf(
    DilemmaMission(
        title = "Gestão de Conflito",
        question = "Vocês tiveram um desentendimento bobo no final do encontro, o clima esfriou um pouco. Qual é o seu movimento na manhã seguinte?",
        options = listOf(
            "Mando uma mensagem longa e bem resolvida pra não deixar o assunto pendente.",
            "Espero a outra pessoa falar pra sentir o clima, não quero forçar nada.",
            "Finjo que nada aconteceu e chamo pra fazer algo divertido pra aliviar a tensão."
        )
    ),
    DilemmaMission(
        title = "Estilo de Comunicação",
        question = "O que você prefere quando a conversa pelo WhatsApp está ficando profunda e complexa?",
        options = listOf(
            "Ligo ou mando um áudio, odeio mal-entendidos por texto.",
            "Deixo as mensagens profundas rendendo ao longo do dia, no nosso tempo.",
            "Paro o assunto e marco de falar sobre isso pessoalmente tomando alguma coisa."
        )
    ),
    DilemmaMission(
        title = "Individualidade",
        question = "Domingo nublado, zero compromissos. Como você descreveria seu cenário ideal estando com alguém?",
        options = listOf(
            "No sofá, maratonando série embaixo da mesma coberta o dia todo.",
            "Cada um num canto da sala fazendo suas coisas, mas curtindo a companhia.",
            "Pegando o carro de última hora pra ir almoçar numa cidade vizinha."
        )
    ),
    DilemmaMission(
        title = "Flexibilidade",
        question = "O encontro estava marcado para daqui a uma hora, mas o lugar fechou por causa da chuva. Qual é a sua reação automática?",
        options = listOf(
            "Tento resolver rápido e sugerir um lugar parecido perto dali pra não perder a noite.",
            "Proponho algo caseiro, um delivery e um filme.",
            "Prefiro remarcar pra outro dia onde a gente possa fazer o plano original direito."
        )
    ),
    DilemmaMission(
        title = "Ritmo do Romance",
        question = "Vocês saíram, foi muito bom, e o assunto rendeu. Como costuma ser sua empolgação nos dias seguintes?",
        options = listOf(
            "Acelero as mensagens, já quero marcar o próximo e fico pensando na pessoa.",
            "Dou um passo de cada vez. Foi bom, mas prefiro manter meu ritmo normal pra ver se sustenta.",
            "Fico esperando a outra pessoa dar sinais mais claros antes de me soltar totalmente."
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DilemmaBottomSheet(
    onDismiss: () -> Unit,
    onDilemmaSelected: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Missões de Conexão",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            connectionMissions.forEach { mission ->
                ElevatedCard(
                    onClick = {
                        onDilemmaSelected(mission.asChatMessage())
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = mission.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mission.question,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
