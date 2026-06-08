package com.ideiassertiva.FypMatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ideiassertiva.FypMatch.ui.navigation.FypMatchNavigation
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FypMatchTheme {
                FypMatchApp()
            }
        }
    }
}

@Composable
fun FypMatchApp() {
    FypMatchNavigation()
}

@Preview(showBackground = true)
@Composable
fun FypMatchAppPreview() {
    FypMatchTheme {
        FypMatchApp()
    }
}
