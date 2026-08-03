package com.ideiassertiva.FypMatch.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.ideiassertiva.FypMatch.ui.screens.*
import com.ideiassertiva.FypMatch.ui.viewmodel.DeepLinkViewModel
import com.ideiassertiva.FypMatch.ui.viewmodel.DiscoveryViewModel
import com.ideiassertiva.FypMatch.ui.viewmodel.OnboardingViewModel
import com.google.firebase.auth.FirebaseAuth

private fun getCurrentUserId(): String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object AppGuide : Screen("app_guide")
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
    object Store : Screen("store")
    object AdvancedFilters : Screen("advanced_filters")
    object AICounselor : Screen("ai_counselor/{userId}") {
        fun createRoute(userId: String) = "ai_counselor/$userId"
    }
    object Phase4AI : Screen("phase4_ai/{userId}") {
        fun createRoute(userId: String) = "phase4_ai/$userId"
    }
    object AccessCode : Screen("access_code")
    object UserDetails : Screen("user_details/{userId}") {
        fun createRoute(userId: String) = "user_details/${Uri.encode(userId)}"
    }
    object CompatibilityDetails : Screen("compatibility_details/{userId}") {
        fun createRoute(userId: String) = "compatibility_details/${Uri.encode(userId)}"
    }
    object ConnectionMap : Screen("connection_map/{userId}") {
        fun createRoute(userId: String) = "connection_map/${Uri.encode(userId)}"
    }
    object Availability : Screen("availability/{userId}") {
        fun createRoute(userId: String) = "availability/${Uri.encode(userId)}"
    }
    object ProfileEdit : Screen("profile_edit")
    /** "Quem viu meu perfil" — visualizações gerais recebidas */
    object ProfileViewers : Screen("profile_viewers")
    /** Tela de configurações de conta — nova Sprint 1 */
    object Settings : Screen("settings")
    /** Central de segurança, bloqueios e diretrizes da comunidade */
    object SafetyCenter : Screen("safety_center")
    /** Revisão manual de selfie para selo de foto verificada */
    object PhotoVerification : Screen("photo_verification")
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
    /** Mapa de Valores — acordos de vida e privacidade LGPD */
    object LifeValues : Screen("life_values/{userId}") {
        fun createRoute(userId: String) = "life_values/$userId"
    }
    object LifeValuesPrivacy : Screen("life_values_privacy/{userId}") {
        fun createRoute(userId: String) = "life_values_privacy/$userId"
    }
    object LifeValuesCompat : Screen("life_values_compat/{currentUserId}/{targetUserId}") {
        fun createRoute(current: String, target: String) = "life_values_compat/$current/$target"
    }
    object AvailabilityPrivacy : Screen("availability_privacy/{userId}") {
        fun createRoute(userId: String) = "availability_privacy/$userId"
    }
    object AvailabilityRecurrence : Screen("availability_recurrence")
    object HabitsRoutine : Screen("habits_routine")
}

private fun postOnboardingStartDestination(): String =
    if (getCurrentUserId().isNotBlank()) Screen.Discovery.route else Screen.Welcome.route

@Composable
fun FypMatchNavigation(
    navController: NavHostController = rememberNavController(),
    pendingProfileUsername: String? = null
) {
    // DiscoveryViewModel compartilhado — conecta ações de swipe de UserDetails ao estado de Discovery
    val discoveryViewModel: DiscoveryViewModel = hiltViewModel()
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val deepLinkViewModel: DeepLinkViewModel = hiltViewModel()
    val isOnboardingDismissed by onboardingViewModel.isOnboardingDismissed.collectAsStateWithLifecycle()

    // Mantém o login: se já existe sessão Firebase, inicia direto na Discovery (pula Welcome/Login)
    val startDestination =
        if (isOnboardingDismissed) postOnboardingStartDestination() else Screen.Onboarding.route

    // Deep link de perfil compartilhado (https://fypmatch.web.app/u/{username}) — resolve
    // username -> userId e navega para a tela de detalhes assim que houver sessão ativa.
    LaunchedEffect(pendingProfileUsername, isOnboardingDismissed) {
        if (!pendingProfileUsername.isNullOrBlank() && getCurrentUserId().isNotBlank()) {
            val userId = deepLinkViewModel.resolveUsername(pendingProfileUsername)
            if (!userId.isNullOrBlank()) {
                navController.navigate(Screen.UserDetails.createRoute(userId))
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = { doNotShowAgain ->
                    onboardingViewModel.finish(doNotShowAgain)
                    navController.navigate(postOnboardingStartDestination()) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.AppGuide.route) {
            OnboardingScreen(
                showDoNotShowAgain = false,
                skipLabel = "Fechar",
                finalActionLabel = "Fechar",
                onFinish = {
                    navController.popBackStack()
                }
            )
        }

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
                    navController.navigate(Screen.ProfileEdit.route) {
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
                },
                viewModel = discoveryViewModel
            )
        }

        composable(Screen.AdvancedFilters.route) {
            AdvancedFiltersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onFiltersApplied = {
                    discoveryViewModel.refreshCards()
                    navController.popBackStack()
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
                    navController.navigate(Screen.EnhancedChat.createRoute(matchId, true))
                }
            )
        }

        // ─── Curtidas recebidas / enviadas / matches ──────────────────────
        composable(Screen.Likes.route) {
            LikesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserDetails = { userId ->
                    navController.navigate(Screen.UserDetails.createRoute(userId))
                },
                onNavigateToChat = { conversationId ->
                    navController.navigate(Screen.EnhancedChat.createRoute(conversationId, true))
                }
            )
        }

        composable(Screen.Conversations.route) {
            ConversationsScreen(
                currentUserId = getCurrentUserId(),
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
                currentUserId = getCurrentUserId(),
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
                currentUserId = getCurrentUserId(),
                useFirebase = useFirebase,
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToCompatibility = { userId ->
                    navController.navigate(Screen.CompatibilityDetails.createRoute(userId))
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
                    navController.navigate(Screen.Store.route)
                }
            )
        }

        composable(Screen.Store.route) {
            StoreScreen(
                userId = getCurrentUserId(),
                onNavigateBack = { navController.popBackStack() }
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
                onActionComplete = {
                    discoveryViewModel.refreshCards()
                    navController.popBackStack()
                },
                onNavigateToChat = { conversationId ->
                    navController.popBackStack()
                    discoveryViewModel.refreshCards()
                    navController.navigate(Screen.EnhancedChat.createRoute(conversationId, true))
                },
                onNavigateToCompatibility = { targetUserId ->
                    navController.navigate(Screen.CompatibilityDetails.createRoute(targetUserId))
                }
            )
        }

        composable(Screen.CompatibilityDetails.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            CompatibilityDetailsScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ProfileEdit.route) {
            ProfileEditScreen(
                onNavigateBack = {
                    navController.navigate(Screen.Discovery.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = false }
                    }
                },
                onSave = { user ->
                    navController.navigate(Screen.Discovery.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = false }
                    }
                }
            )
        }

        // ─── Configurações de conta ───────────────────────────────────────
        composable(Screen.Settings.route) {
            val uid = getCurrentUserId()
            SettingsScreen(
                onNavigateToPremium = {
                    navController.navigate(Screen.Premium.route)
                },
                onNavigateToStore = {
                    navController.navigate(Screen.Store.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.ProfileEdit.route)
                },
                onNavigateToFilters = {
                    navController.navigate(Screen.AdvancedFilters.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSelfKnowledge = {
                    navController.navigate(Screen.SelfKnowledge.createRoute(uid))
                },
                onNavigateToConnectionMap = {
                    val resolvedUid = getCurrentUserId()
                    if (resolvedUid.isNotBlank()) {
                        navController.navigate(Screen.ConnectionMap.createRoute(resolvedUid))
                    }
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
                },
                onNavigateToPhotoVerification = {
                    navController.navigate(Screen.PhotoVerification.route)
                },
                onNavigateToAvailability = {
                    if (uid.isNotBlank()) {
                        navController.navigate(Screen.Availability.createRoute(uid))
                    }
                },
                onNavigateToLifeValues = {
                    if (uid.isNotBlank()) {
                        navController.navigate(Screen.LifeValues.createRoute(uid))
                    }
                },
                onNavigateToAppGuide = {
                    navController.navigate(Screen.AppGuide.route)
                },
                onNavigateToSafetyCenter = {
                    navController.navigate(Screen.SafetyCenter.route)
                },
                onNavigateToProfileViewers = {
                    navController.navigate(Screen.ProfileViewers.route)
                }
            )
        }

        composable(Screen.ProfileViewers.route) {
            ProfileViewersScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserDetails = { userId ->
                    navController.navigate(Screen.UserDetails.createRoute(userId))
                }
            )
        }

        composable(Screen.SafetyCenter.route) {
            SafetyCenterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.LifeValues.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = getCurrentUserId().ifBlank {
                backStackEntry.arguments?.getString("userId").orEmpty()
            }
            LifeValuesMapScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() },
                onNavigatePrivacy = {
                    navController.navigate(Screen.LifeValuesPrivacy.createRoute(userId))
                }
            )
        }

        composable(
            route = Screen.LifeValuesCompat.route,
            arguments = listOf(
                navArgument("currentUserId") { type = NavType.StringType },
                navArgument("targetUserId") { type = NavType.StringType }
            )
        ) { entry ->
            LifeValuesCompatibilityScreen(
                currentUserId = entry.arguments?.getString("currentUserId").orEmpty(),
                targetUserId = entry.arguments?.getString("targetUserId").orEmpty(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.LifeValuesPrivacy.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { entry ->
            val userId = getCurrentUserId().ifBlank {
                entry.arguments?.getString("userId").orEmpty()
            }
            LifeValuesPrivacyScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AvailabilityPrivacy.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { entry ->
            AvailabilityPrivacyScreen(
                userId = entry.arguments?.getString("userId").orEmpty(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AvailabilityRecurrence.route) {
            AvailabilityRecurrenceScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.HabitsRoutine.route) {
            HabitsRoutineScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Availability.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            AvailabilityCalendarScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ConnectionMap.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ConnectionMapScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuickMode = { navController.navigate(Screen.QuickMode.createRoute(userId)) },
                onNavigateToDeepMode = { navController.navigate(Screen.DeepMode.createRoute(userId)) },
                onNavigateToSelfKnowledge = { navController.navigate(Screen.SelfKnowledge.createRoute(userId)) }
            )
        }

        composable(Screen.PhotoVerification.route) {
            PhotoVerificationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = {
                    navController.navigate(Screen.ProfileEdit.route)
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
            val uid = getCurrentUserId()
            val viewModel: com.ideiassertiva.FypMatch.ui.viewmodel.NeuroProfileViewModel = hiltViewModel()
            NeuroProfileScreen(
                userId = uid,
                onNavigateBack = { navController.popBackStack() },
                onComplete = { profile ->
                    viewModel.save(profile.copy(userId = uid)) {
                        navController.popBackStack()
                    }
                }
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
                onNavigateToConnectionMap = {
                    val uid = getCurrentUserId().ifBlank { userId }
                    if (uid.isNotBlank()) {
                        navController.navigate(Screen.ConnectionMap.createRoute(uid))
                    }
                },
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
                onNavigateToConnectionMap = {
                    val uid = getCurrentUserId().ifBlank { userId }
                    if (uid.isNotBlank()) {
                        navController.navigate(Screen.ConnectionMap.createRoute(uid))
                    }
                },
                userId = userId
            )
        }
    }
}
