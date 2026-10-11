package com.example.nd_mobpriet.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = LightGreen,
    onPrimaryContainer = Color(0xFF183D29),
    secondary = Color(0xFF626A40),
    secondaryContainer = Color(0xFFE9EDDA),
    onSecondaryContainer = Color(0xFF343B1C),
    background = Cream,
    surface = Cream,
    surfaceContainer = Color(0xFFEEF2E9),
    surfaceVariant = Color(0xFFE7ECE2),
    onSurface = Color(0xFF202920)
)

private val DarkColors = darkColorScheme(
    primary = DarkGreen,
    primaryContainer = Color(0xFF254F36),
    onPrimaryContainer = LightGreen,
    secondary = Color(0xFFC4CD9E),
    secondaryContainer = Color(0xFF3E472B),
    background = Charcoal,
    surface = Charcoal,
    surfaceContainer = Color(0xFF202A23),
    surfaceVariant = Color(0xFF39453B)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun ND_MobPrietTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
