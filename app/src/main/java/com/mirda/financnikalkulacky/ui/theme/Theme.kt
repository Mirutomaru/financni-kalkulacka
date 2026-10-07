package com.mirda.financnikalkulacky.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.mirda.financnikalkulacky.R

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
    MaterialTheme(colorScheme = GreenColorScheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.bg_finance),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            // Jemné ztmavení nad obrázkem — bílé písmo je čitelné i na světlých partiích.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
            )
            content()
        }
    }
}
