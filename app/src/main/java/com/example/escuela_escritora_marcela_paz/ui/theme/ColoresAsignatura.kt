package com.example.escuela_escritora_marcela_paz.ui.theme

import androidx.compose.ui.graphics.Color

/** Un color suave por asignatura, para reconocerlas rápido (niños de 2.º básico). */
object ColoresAsignatura {
    fun fondo(asignatura: String): Color = when {
        asignatura.startsWith("Lenguaje") -> Color(0xFFFFEEF3)
        asignatura.startsWith("Matem") -> Color(0xFFEDF5FF)
        asignatura.startsWith("Ciencias") -> Color(0xFFEAF8F0)
        else -> Color(0xFFFFF7E0)
    }

    fun principal(asignatura: String): Color = when {
        asignatura.startsWith("Lenguaje") -> Color(0xFFB83B66)
        asignatura.startsWith("Matem") -> Color(0xFF3169C6)
        asignatura.startsWith("Ciencias") -> Color(0xFF188272)
        else -> Color(0xFF9A6200)
    }
}