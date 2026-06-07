package com.ideiassertiva.FypMatch.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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

            val dilemmas = listOf(
                "Vocês tiveram um desentendimento bobo no final do encontro, o clima esfriou um pouco. Qual é o seu movimento na manhã seguinte?",
                "O que você prefere quando a conversa pelo WhatsApp está ficando profunda e complexa?",
                "Domingo nublado, zero compromissos. Como você descreveria seu cenário ideal estando com alguém?",
                "O encontro estava marcado para daqui a uma hora, mas o lugar fechou por causa da chuva. Qual é a sua reação automática?",
                "Vocês saíram, foi muito bom, e o assunto rendeu. Como costuma ser sua empolgação nos dias seguintes?"
            )

            dilemmas.forEachIndexed { index, dilemma ->
                OutlinedButton(
                    onClick = { 
                        onDilemmaSelected("🎮 Missão de Conexão: $dilemma\n\nA) Resposta A\nB) Resposta B\nC) Resposta C")
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text("Dilema ${index + 1}: ${dilemma.take(30)}...")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
