package de.sofoste.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Void = Color(0xFF050714)
val DeepSpace = Color(0xFF0B1027)
val Panel = Color(0xFF111833)
val Starlight = Color(0xFFF6F2FF)
val Moonlight = Color(0xFFC5C5D8)
val Dust = Color(0xFF8E91AC)
val Aurora = Color(0xFF69E7FF)
val Orbit = Color(0xFF9B7AFF)
val Nebula = Color(0xFFF07BC5)
val Solar = Color(0xFFF4C875)
val Danger = Color(0xFFFF8D9B)

private val SofosteColors = darkColorScheme(
    primary = Aurora,
    onPrimary = Void,
    secondary = Orbit,
    onSecondary = Void,
    tertiary = Nebula,
    background = Void,
    onBackground = Starlight,
    surface = DeepSpace,
    onSurface = Starlight,
    surfaceVariant = Panel,
    onSurfaceVariant = Moonlight,
    error = Danger,
    onError = Void,
)

@Composable
fun SofosteTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SofosteColors,
        typography = Typography(),
        content = content,
    )
}
