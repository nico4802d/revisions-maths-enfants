package com.nico.revisionsmaths.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Indigo = Color(0xFF3F51B5)
private val Orange = Color(0xFFFF7043)
private val Green = Color(0xFF43A047)

private val LightColors = lightColorScheme(
    primary = Indigo,
    secondary = Orange,
    tertiary = Green
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FA8DA),
    secondary = Color(0xFFFF8A65),
    tertiary = Color(0xFF81C784)
)

@Composable
fun RevisionsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
