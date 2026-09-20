package de.sofoste.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import de.sofoste.app.ui.SofosteApp
import de.sofoste.app.ui.theme.SofosteTheme
import de.sofoste.app.notifications.TritonNotifications

class MainActivity : ComponentActivity() {
    private val studentActivityRequest = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action == TritonNotifications.OPEN_STUDENT_ACTIVITY) {
            studentActivityRequest.value++
        }
        enableEdgeToEdge()
        setContent {
            SofosteTheme {
                SofosteApp(studentActivityRequest = studentActivityRequest.value)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == TritonNotifications.OPEN_STUDENT_ACTIVITY) {
            studentActivityRequest.value++
        }
    }
}
