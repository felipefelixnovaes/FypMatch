package com.ideiassertiva.FypMatch.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme
import com.ideiassertiva.FypMatch.ui.theme.MatchPink40
import com.ideiassertiva.FypMatch.ui.theme.MatchPurple40
import kotlinx.coroutines.delay

// Dados dos slides de onboarding — copy aprovado pelo Squad de Copy
private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val body: String
)

private val onboardingPages = listOf(
    OnboardingPage(
        emoji = "💕",
        title = "Bem-vindo ao FypMatch",
        body = "O app de relacionamento criado para quem nunca se sentiu em casa nos outros."
    ),
    OnboardingPage(
        emoji = "🏳️‍🌈",
        title = "Um espaço feito para você",
        body = "LGBTQIA+, neurodiverso ou simplesmente cansado da superficialidade? Aqui você é bem-vindo do jeito que é."
    ),
    OnboardingPage(
        emoji = "🧠",
        title = "Matches que fazem sentido",
        body = "Nosso score analisa personalidade, valores e intenções — não só fotos. Menos swipes, mais conexões reais."
    ),
    OnboardingPage(
        emoji = "🤖",
        title = "Você não está sozinho",
        body = "Seu conselheiro IA está sempre aqui. Para sugerir icebreakers, ajudar na conversa ou apoiar quando a ansiedade aparecer."
    ),
    OnboardingPage(
        emoji = "✨",
        title = "Pronto para começar?",
        body = "Junte-se a milhares de pessoas encontrando conexões reais todos os dias. Seu match está esperando."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    onNavigateToWaitlist: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToAccessCode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val gradient = Brush.verticalGradient(listOf(MatchPink40, MatchPurple40))
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })

    // Auto-scroll do carousel a cada 3 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % onboardingPages.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // Logo
            Text(
                text = "💕",
                fontSize = 72.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "FypMatch",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Conexões Reais",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // Carousel de onboarding
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) { page ->
                OnboardingCard(
                    page = onboardingPages[page],
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(16.dp))

            // Indicadores de página
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White
                                else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // CTAs
            Button(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MatchPink40
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Criar conta grátis",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = OutlinedButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Já tenho conta",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onNavigateToWaitlist) {
                    Text("Entrar na lista", color = Color.White.copy(alpha = 0.85f))
                }
                Text("·", color = Color.White.copy(alpha = 0.5f))
                TextButton(onClick = onNavigateToAccessCode) {
                    Text("Tenho um código", color = Color.White.copy(alpha = 0.85f))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// Card individual do carousel de onboarding
@Composable
private fun OnboardingCard(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = page.emoji,
                fontSize = 40.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = page.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = page.body,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.85f)
                ),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WelcomeScreenPreview() {
    FypMatchTheme {
        WelcomeScreen()
    }
}
