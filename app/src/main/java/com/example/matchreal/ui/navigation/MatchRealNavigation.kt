package com.example.matchreal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.matchreal.ui.screens.AccessCodeScreen
import com.example.matchreal.ui.screens.AICounselorScreen
import com.example.matchreal.ui.screens.DiscoveryScreen
import com.example.matchreal.ui.screens.LoginScreen
import com.example.matchreal.ui.screens.MatchesScreen
import com.example.matchreal.ui.screens.PremiumScreen
import com.example.matchreal.ui.screens.ProfileScreen
import com.example.matchreal.ui.screens.WaitlistScreen
import com.example.matchreal.ui.screens.WelcomeScreen

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Waitlist : Screen("waitlist") 
    object Login : Screen("login")
    object Discovery : Screen("discovery")
    object Profile : Screen("profile")
    object Matches : Screen("matches")
    object Chat : Screen("chat/{matchId}") {
        fun createRoute(matchId: String) = "chat/$matchId"
    }
    object Premium : Screen("premium")
    object AICounselor : Screen("ai_counselor/{userId}") {
        fun createRoute(userId: String) = "ai_counselor/$userId"
    }
    object AccessCode : Screen("access_code")
}

@Composable
fun MatchRealNavigation(
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
                    navController.navigate(Screen.Matches.route)
                },
                onNavigateToPremium = {
                    navController.navigate(Screen.Premium.route)
                },
                onNavigateToAICounselor = { userId ->
                    navController.navigate(Screen.AICounselor.createRoute(userId))
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
        
        composable(Screen.Chat.route) {
            // TODO: Implementar ChatScreen
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
    }
} 