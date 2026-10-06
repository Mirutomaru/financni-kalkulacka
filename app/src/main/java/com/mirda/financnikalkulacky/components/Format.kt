package com.mirda.financnikalkulacky.components

import kotlin.math.roundToLong

/** Formát částky v Kč s mezerami jako tisíciřadami: 1234567 -> "1 234 567 Kč". */
fun fmtCzk(value: Double): String {
    val rounded = value.roundToLong()
    return String.format("%,d", rounded).replace(",", " ") + " Kč"
}
