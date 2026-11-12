package com.nico.revisionsmaths.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple = Color(0xFF7C4DFF)
val PurpleLight = Color(0xFFB388FF)
val Pink = Color(0xFFFF4081)
val Teal = Color(0xFF00BFA5)
val Amber = Color(0xFFFFB300)
val Orange = Color(0xFFFF7043)
val Green = Color(0xFF43A047)
val Red = Color(0xFFE53935)

val ProfileCardColors = listOf(
    Color(0xFFEDE7FF),
    Color(0xFFFFF3E0),
    Color(0xFFE0F7FA),
    Color(0xFFFCE4EC),
    Color(0xFFE8F5E9),
    Color(0xFFF3E5F5)
)

val ProfileAvatars = listOf("🦊", "🐼", "🦁", "🐸", "🐨", "🦄", "🐧", "🐯")

private val LightColors = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE7FF),
    onPrimaryContainer = Color(0xFF311B92),
    secondary = Pink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E8),
    onSecondaryContainer = Color(0xFF880E4F),
    tertiary = Teal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCF8EF),
    onTertiaryContainer = Color(0xFF004D40),
    background = Color(0xFFF7F4FF),
    surface = Color.White,
    surfaceVariant = Color(0xFFEDEAF6),
    error = Red
)

private val DarkColors = darkColorScheme(
    primary = PurpleLight,
    onPrimary = Color(0xFF1A004D),
    primaryContainer = Color(0xFF4B2CB3),
    onPrimaryContainer = Color(0xFFEDE7FF),
    secondary = Color(0xFFFF80AB),
    onSecondary = Color(0xFF4A0027),
    secondaryContainer = Color(0xFF880E4F),
    onSecondaryContainer = Color(0xFFFFD9E8),
    tertiary = Color(0xFF64FFDA),
    onTertiary = Color(0xFF00332B),
    tertiaryContainer = Color(0xFF004D40),
    onTertiaryContainer = Color(0xFFCCF8EF),
    background = Color(0xFF16162A),
    surface = Color(0xFF1E1E36),
    surfaceVariant = Color(0xFF2A2A4A),
    error = Color(0xFFFF5252)
)

@Composable
fun RevisionsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
