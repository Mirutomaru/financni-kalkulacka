package com.mirda.financnikalkulacky.components

import kotlin.math.roundToLong

/** Formát částky v Kč s mezerami jako tisíciřadami: 1234567 -> "1 234 567 Kč". */
fun fmtCzk(value: Double): String {
    val rounded = value.roundToLong()
    return String.format("%,d", rounded).replace(",", " ") + " Kč"
}

/** Formát čísla s mezerami jako tisíciřadami (bez měny), max. 2 desetinná místa. */
fun fmtNum(value: Double): String {
    val text = if (value == value.roundToLong().toDouble()) {
        String.format("%,d", value.roundToLong()).replace(",", " ")
    } else {
        String.format("%.2f", value).replace(".", ",").replace(Regex("(?<=\\d)(?=\\d{3})"), " ")
    }
    return text
}
