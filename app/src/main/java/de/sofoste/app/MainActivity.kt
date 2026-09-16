package de.sofoste.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.sofoste.app.ui.SofosteApp
import de.sofoste.app.ui.theme.SofosteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SofosteTheme {
                SofosteApp()
            }
        }
    }
}
