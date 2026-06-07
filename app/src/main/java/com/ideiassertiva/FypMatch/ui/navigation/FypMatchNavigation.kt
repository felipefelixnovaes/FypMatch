package com.ideiassertiva.FypMatch.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.ideiassertiva.FypMatch.model.SwipeType
import com.ideiassertiva.FypMatch.ui.screens.*
import com.ideiassertiva.FypMatch.ui.viewmodel.DiscoveryViewModel

/** Placeholder de userId — substituir por auth state real (FirebaseAuth.currentUser.uid) */
private const val PLACEHOLDER_USER_ID = "placeholder_user"

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Waitlist : Screen("waitlist")
    object Login : Screen("login")
    /** Tela de cadastro com email/senha — nova Sprint 1 */
    object Register : Screen("register")
    /** Programa de afiliados — Sprint 3 */
    object Affiliate : Screen("affiliate")
    /** Ganhar créditos IA assistindo anúncios — Sprint 3 */
    object Ads : Screen("ads/{userId}") {
        fun createRoute(userId: String) = "ads/$userId"
    }
    object Discovery : Screen("discovery")
    object Profile : Screen("profile")
    object Matches : Screen("matches")
    /** Curtidas recebidas / enviadas / matches */
    object Likes : Screen("likes")
    object Conversations : Screen("conversations")
    object Chat : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String) = "chat/$conversationId"
    }
    object EnhancedChat : Screen("enhanced_chat/{conversationId}/{useFirebase}") {
        fun createRoute(conversationId: String, useFirebase: Boolean = true) = "enhanced_chat/$conversationId/$useFirebase"
    }
    object Phase3Demo : Screen("phase3_demo")
    object Premium : Screen("premium")
    object AICounselor : Screen("ai_counselor/{userId}") {
        fun createRoute(userId: String) = "ai_counselor/$userId"
    }
    object Phase4AI : Screen("phase4_ai/{userId}") {
        fun createRoute(userId: String) = "phase4_ai/$userId"
    }
    object AccessCode : Screen("access_code")
    object UserDetails : Screen("user_details/{userId}") {
        fun createRoute(userId: String) = "user_details/$userId"
    }
    object ProfileEdit : Screen("profile_edit")
    /** Tela de configurações de conta — nova Sprint 1 */
    object Settings : Screen("settings")
    /** Questionário de perfil de neurodiversidade */
    object NeuroProfile : Screen("neuro_profile")
    /** Perfil complementar importado de uma IA externa do usuário */
    object ComplementaryProfile : Screen("complementary_profile")
    /** Hub de suporte para usuários neurodivergentes */
    object NeuroSupport : Screen("neuro_support")
    /** Questionário de compatibilidade — Modo Rápido */
    object QuickMode : Screen("quick_mode/{userId}") {
        fun createRoute(userId: String) = "quick_mode/$userId"
    }
    /** Questionário de compatibilidade — Modo Profundo (Sprint 6) */
    object DeepMode : Screen("deep_mode/{userId}") {
        fun createRoute(userId: String) = "deep_mode/$userId"
    }
    /** Questionário de Eneagrama — Modo Autoconhecimento (Sprint 7a) */
    object Enneagram : Screen("enneagram/{userId}") {
        fun createRoute(userId: String) = "enneagram/$userId"
    }
    /** Questionário de Linguagem do Cuidado — Modo Autoconhecimento (Sprint 7b) */
    object LoveLanguage : Screen("love_language/{userId}") {
        fun createRoute(u: String) = "love_language/$u"
    }
    /** Questionário de Arquétipo — Modo Autoconhecimento (Sprint 7b) */
    object Archetype : Screen("archetype/{userId}") {
        fun createRoute(u: String) = "archetype/$u"
    }
    /** Hub de Autoconhecimento — Modo Autoconhecimento (Sprint 7b) */
    object SelfKnowledge : Screen("self_knowledge/{userId}") {
        fun createRoute(u: String) = "self_knowledge/$u"
    }
}

@Composable
fun FypMatchNavigation(
    navController: NavHostController = rememberNavController()
) {
    // DiscoveryViewModel compartilhado — conecta ações de swipe de UserDetails ao estado de Discovery
    val discoveryViewModel: DiscoveryViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToWaitlist = {
                    navController.navigate(Screen.Waitlist.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToAccessCode = {
                    navController.navigate(Screen.AccessCode.route)
                }
            )
        }

        composable(Screen.Waitlist.route) {
            WaitlistScreen()
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToDiscovery = {
                    navController.navigate(Screen.Discovery.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ─── Cadastro com email/senha ─────────────────────────────────────
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDiscovery = {
                    navController.navigate(Screen.Discovery.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Discovery.route) {
            DiscoveryScreen(
                onNavigateToMatches = {
                    navController.navigate(Screen.Likes.route)
                },
                onNavigateToPremium = {
                    navController.navigate(Screen.Premium.route)
                },
                onNavigateToAICounselor = { userId ->
                    navController.navigate(Screen.AICounselor.createRoute(userId))
                },
                onNavigateToPhase4AI = { userId ->
                    navController.navigate(Screen.Phase4AI.createRoute(userId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.ProfileEdit.route)
                },
                onNavigateToUserDetails = { userId ->
                    navController.navigate(Screen.UserDetails.createRoute(userId))
                },
                onNavigateToChat = { conversationId ->
                    navController.navigate(Screen.EnhancedChat.createRoute(conversationId, true))
                },
                onNavigateToPhase3Demo = {
                    navController.navigate(Screen.Phase3Demo.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToDiscovery = {
                    navController.navigate(Screen.Discovery.route) {
                        popUpTo(Screen.Profile.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Matches.route) {
            MatchesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChat = { matchId ->
                    navController.navigate(Screen.Chat.createRoute(matchId))
                }
            )
        }

        // ─── Curtidas recebidas / enviadas / matches ──────────────────────
        composable(Screen.Likes.route) {
            LikesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserDetails = { userId ->
                    navController.navigate(Screen.UserDetails.createRoute(userId))
                }
            )
        }

        composable(Screen.Conversations.route) {
            ConversationsScreen(
                currentUserId = PLACEHOLDER_USER_ID, // Em um app real, viria da autenticação
                onConversationClick = { conversationId ->
                    navController.navigate(Screen.EnhancedChat.createRoute(conversationId, true))
                },
                onNavigateToPhase3Demo = {
                    navController.navigate(Screen.Phase3Demo.route)
                }
            )
        }

        composable(Screen.Chat.route) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatScreen(
                conversationId = conversationId,
                currentUserId = PLACEHOLDER_USER_ID, // Em um app real, viria da autenticação
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EnhancedChat.route) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val useFirebase = backStackEntry.arguments?.getString("useFirebase")?.toBooleanStrictOrNull() ?: true
            EnhancedChatScreen(
                conversationId = conversationId,
                currentUserId = PLACEHOLDER_USER_ID, // Em um app real, viria da autenticação
                useFirebase = useFirebase,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Phase3Demo.route) {
            Phase3DemoScreen(
                onNavigateToMockChat = { conversationId, userId ->
                    navController.navigate(Screen.EnhancedChat.createRoute(conversationId, false))
                },
                onNavigateToFirebaseChat = { conversationId, userId ->
                    navController.navigate(Screen.EnhancedChat.createRoute(conversationId, true))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Premium.route) {
            PremiumScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPurchase = { subscriptionStatus ->
                    // Após confirmação de compra, volta para a tela anterior
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AICounselor.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            AICounselorScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                userId = userId,
                onNavigateToComplementaryProfile = {
                    navController.navigate(Screen.ComplementaryProfile.route)
                }
            )
        }

        composable(Screen.Phase4AI.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            Phase4AIScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                userId = userId
            )
        }

        composable(Screen.AccessCode.route) {
            AccessCodeScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    // Após resgatar o código, vai para a tela principal (Discovery)
                    navController.navigate(Screen.Discovery.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.UserDetails.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserDetailsScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLike = {
                    discoveryViewModel.performSwipe(SwipeType.LIKE)
                    navController.popBackStack()
                },
                onPass = {
                    discoveryViewModel.performSwipe(SwipeType.PASS)
                    navController.popBackStack()
                },
                onSuperLike = {
                    discoveryViewModel.performSwipe(SwipeType.SUPER_LIKE)
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ProfileEdit.route) {
            ProfileEditScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSave = { user ->
                    // Salva e volta para a tela anterior
                    navController.popBackStack()
                }
            )
        }

        // ─── Configurações de conta ───────────────────────────────────────
        composable(Screen.Settings.route) {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: PLACEHOLDER_USER_ID
            SettingsScreen(
                onNavigateToPremium = {
                    navController.navigate(Screen.Premium.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSelfKnowledge = {
                    navController.navigate(Screen.SelfKnowledge.createRoute(uid))
                },
                onNavigateToQuickMode = {
                    navController.navigate(Screen.QuickMode.createRoute(uid))
                },
                onNavigateToDeepMode = {
                    navController.navigate(Screen.DeepMode.createRoute(uid))
                },
                onNavigateToAffiliate = {
                    navController.navigate(Screen.Affiliate.route)
                },
                onNavigateToAds = {
                    navController.navigate(Screen.Ads.createRoute(uid))
                },
                onNavigateToNeuroProfile = {
                    navController.navigate(Screen.NeuroProfile.route)
                },
                onNavigateToComplementaryProfile = {
                    navController.navigate(Screen.ComplementaryProfile.route)
                }
            )
        }

        // ─── Programa de afiliados — Sprint 3 ────────────────────────────
        composable(Screen.Affiliate.route) {
            AffiliateScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ─── Perfil de Neurodiversidade ───────────────────────────────────
        composable(Screen.NeuroProfile.route) {
            NeuroProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onComplete = { navController.popBackStack() }
            )
        }

        composable(Screen.ComplementaryProfile.route) {
            ComplementaryProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ─── Hub de Suporte Neuro ─────────────────────────────────────────
        composable(Screen.NeuroSupport.route) {
            NeuroSupportScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNeuroProfile = { navController.navigate(Screen.NeuroProfile.route) }
            )
        }

        // ─── Créditos IA via anúncios — Sprint 3 ─────────────────────────
        composable(
            route = Screen.Ads.route,
            arguments = listOf(androidx.navigation.navArgument("userId") {
                type = androidx.navigation.NavType.StringType
            })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            AdsScreen(
                onNavigateBack = { navController.popBackStack() },
                userId = userId
            )
        }

        // ─── Questionário de compatibilidade — Modo Rápido ────────────────
        composable(
            route = Screen.QuickMode.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            QuickModeScreen(
                onNavigateBack = { navController.popBackStack() },
                onComplete = { navController.popBackStack() },
                userId = userId
            )
        }

        // ─── Questionário de compatibilidade — Modo Profundo (Sprint 6) ───
        composable(
            route = Screen.DeepMode.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            DeepModeScreen(
                onNavigateBack = { navController.popBackStack() },
                onComplete = { navController.popBackStack() },
                userId = userId
            )
        }

        // ─── Eneagrama — Modo Autoconhecimento (Sprint 7a) ────────────────
        composable(
            route = Screen.Enneagram.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            EnneagramScreen(
                onNavigateBack = { navController.popBackStack() },
                onComplete = { navController.popBackStack() },
                userId = userId
            )
        }

        // ─── Linguagem do Cuidado — Modo Autoconhecimento (Sprint 7b) ─────
        composable(
            route = Screen.LoveLanguage.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            LoveLanguageScreen(
                onNavigateBack = { navController.popBackStack() },
                onComplete = { navController.popBackStack() },
                userId = userId
            )
        }

        // ─── Arquétipo — Modo Autoconhecimento (Sprint 7b) ────────────────
        composable(
            route = Screen.Archetype.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ArchetypeScreen(
                onNavigateBack = { navController.popBackStack() },
                onComplete = { navController.popBackStack() },
                userId = userId
            )
        }

        // ─── Hub de Autoconhecimento — Modo Autoconhecimento (Sprint 7b) ──
        composable(
            route = Screen.SelfKnowledge.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            SelfKnowledgeScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartEnneagram = { navController.navigate(Screen.Enneagram.createRoute(userId)) },
                onStartLoveLanguage = { navController.navigate(Screen.LoveLanguage.createRoute(userId)) },
                onStartArchetype = { navController.navigate(Screen.Archetype.createRoute(userId)) },
                userId = userId
            )
        }
    }
}
