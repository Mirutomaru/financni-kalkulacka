package com.mirda.financnikalkulacky.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Brand barvy — přírodní tmavě zelená dle zadání (#0E4D3A). */
val BrandGreen = Color(0xFF0E4D3A)
val BrandGreenDark = Color(0xFF0B3F2F)
val BrandTile = Color(0xFF0A3D2C)
val BrandAccent = Color(0xFF7CB98A)

private val GreenColorScheme = darkColorScheme(
    primary = BrandAccent,
    onPrimary = Color.Black,
    secondary = BrandGreenDark,
    onSecondary = Color.White,
    tertiary = Color(0xFFB5D6C2),
    background = BrandGreen,
    onBackground = Color.White,
    surface = BrandTile,
    onSurface = Color.White,
    surfaceVariant = BrandGreenDark,
    onSurfaceVariant = Color(0xFFCFE8DC),
)

@Composable
fun FinancniKalkulackyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = GreenColorScheme, content = content)
}
