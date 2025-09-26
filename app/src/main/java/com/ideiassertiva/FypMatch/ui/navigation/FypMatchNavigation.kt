package com.ideiassertiva.FypMatch.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ideiassertiva.FypMatch.ui.screens.*

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Waitlist : Screen("waitlist") 
    object Login : Screen("login")
    object Discovery : Screen("discovery")
    object Profile : Screen("profile")
    object Matches : Screen("matches")
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
}

@Composable
fun FypMatchNavigation(
    navController: NavHostController = rememberNavController()
) {
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
        
        composable(Screen.Discovery.route) {
            DiscoveryScreen(
                onNavigateToMatches = {
                    navController.navigate(Screen.Conversations.route)
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
        
        composable(Screen.Conversations.route) {
            ConversationsScreen(
                currentUserId = "current_user_123", // Em um app real, viria da autenticação
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
                currentUserId = "current_user_123", // Em um app real, viria da autenticação
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
                currentUserId = "current_user_123", // Em um app real, viria da autenticação
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
                    // TODO: Implementar compra
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
                userId = userId
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
                    // TODO: Implementar ação de curtir
                    navController.popBackStack()
                },
                onPass = {
                    // TODO: Implementar ação de passar
                    navController.popBackStack()
                },
                onSuperLike = {
                    // TODO: Implementar ação de super curtir
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
                    // TODO: Implementar salvamento do perfil
                    navController.popBackStack()
                }
            )
        }
    }
} 
