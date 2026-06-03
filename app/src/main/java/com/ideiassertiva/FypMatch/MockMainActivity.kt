package com.ideiassertiva.FypMatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme
import com.ideiassertiva.FypMatch.ui.screens.AICounselorContent
import com.ideiassertiva.FypMatch.ui.viewmodel.AICounselorUiState
import com.ideiassertiva.FypMatch.model.*

class MockMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FypMatchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AICounselorContent(
                        uiState = AICounselorUiState(
                            hasActiveSession = true,
                            currentMessage = "Olá, estou testando no emulador (MOCK)."
                        ),
                        currentSession = CounselorSession(
                            messages = listOf(
                                CounselorMessage(content = "Olá! Esta é uma versão mockada para evitar erros de Hilt/Firebase no emulador.", sender = MessageSender.AI_COUNSELOR),
                                CounselorMessage(content = "Perfeito, agora posso ver se a UI renderiza sem erros.", sender = MessageSender.USER),
                                CounselorMessage(content = "A interface foi refatorada para suportar State Hoisting, o que resolveu o problema original de renderização.", sender = MessageSender.AI_COUNSELOR)
                            )
                        ),
                        isLoading = false,
                        userCredits = 25,
                        canWatchAd = true,
                        onNavigateBack = { finish() },
                        onUpdateMessage = { },
                        onSendMessage = { },
                        onWatchAd = { },
                        onDismissAdModal = { }
                    )
                }
            }
        }
    }
}
