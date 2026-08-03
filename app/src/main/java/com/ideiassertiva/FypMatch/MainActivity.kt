package com.ideiassertiva.FypMatch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ideiassertiva.FypMatch.ui.navigation.FypMatchNavigation
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme
import com.ideiassertiva.FypMatch.ui.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    private var pendingProfileUsername = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestNotificationPermission()
        pendingProfileUsername.value = extractProfileUsername(intent)
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            val username by pendingProfileUsername

            FypMatchTheme(darkTheme = themeMode.isDark) {
                FypMatchApp(pendingProfileUsername = username)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingProfileUsername.value = extractProfileUsername(intent)
    }

    // Deep link de perfil: https://fypmatch-8ac3c.web.app/u/{username} (App Links)
    private fun extractProfileUsername(intent: Intent?): String? {
        val data = intent?.data ?: return null
        if (data.host != "fypmatch-8ac3c.web.app") return null
        val segments = data.pathSegments
        if (segments.size != 2 || segments[0] != "u") return null
        return segments[1]
    }

    // Android 13+ exige permissao em runtime para exibir notificacoes (push).
    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun FypMatchApp(pendingProfileUsername: String? = null) {
    FypMatchNavigation(pendingProfileUsername = pendingProfileUsername)
}

@Preview(showBackground = true)
@Composable
fun FypMatchAppPreview() {
    FypMatchTheme {
        FypMatchApp()
    }
}
