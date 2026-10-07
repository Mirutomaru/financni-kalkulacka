package com.mirda.financnikalkulacky.components

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.mirda.financnikalkulacky.ui.model.ChartSeries

/** Jednoduchý line chart: legenda + křivky + vodorovné mřížka + popisky let na ose X. */
@Composable
fun LineChart(series: List<ChartSeries>, modifier: Modifier = Modifier, height: androidx.compose.ui.unit.Dp = 170.dp) {
    val allPoints = series.flatMap { it.points }
    if (allPoints.isEmpty()) return

    val maxX = allPoints.maxOf { it.x }.coerceAtLeast(1f)
    val maxY = allPoints.maxOf { it.y }.coerceAtLeast(1.0)
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f))

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Legenda
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            series.forEach { s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).background(s.color, CircleShape))
                    Spacer(Modifier.width(5.dp))
                    Text(s.label, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val padL = 6f
            val padR = 6f
            val padT = 8f
            val padB = 26f
            val w = size.width - padL - padR
            val h = size.height - padT - padB

            fun px(x: Float) = padL + (x / maxX) * w
            fun py(y: Double) = padT + h - (y.toFloat() / maxY.toFloat()) * h

            // Vodorovná mřížka (4 linky)
            val gridColor = Color.White.copy(alpha = 0.15f)
            for (i in 0..3) {
                val gy = padT + h * i / 3f
                drawLine(gridColor, Offset(padL, gy), Offset(size.width - padR, gy), 1f)
            }

            // Křivky
            series.forEach { s ->
                if (s.points.size < 2) return@forEach
                val path = Path().apply {
                    moveTo(px(s.points.first().x), py(s.points.first().y))
                    for (p in s.points.drop(1)) lineTo(px(p.x), py(p.y))
                }
                drawPath(path, s.color, style = Stroke(width = 3f, cap = StrokeCap.Round))
            }

            // Popisky let na ose X (každých ~5 let)
            val stepYears = if (maxX > 20f) 10f else 5f
            var xYear = 0f
            while (xYear <= maxX + 0.01f) {
                val label = "${xYear.toInt()}"
                val layout = textMeasurer.measure(label, labelStyle)
                drawText(layout, topLeft = Offset(px(xYear) - layout.size.width / 2f, size.height - padB + 6f))
                xYear += stepYears
            }
        }
    }
}
