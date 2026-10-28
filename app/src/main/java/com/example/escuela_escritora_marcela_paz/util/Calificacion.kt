package com.example.escuela_escritora_marcela_paz.util

import java.util.Locale
import kotlin.math.roundToInt

/** Escala chilena 1,0 a 7,0 con 60 % de exigencia (60 % correcto = nota 4,0). */
object Calificacion {

    fun nota(aciertos: Int, total: Int): Double {
        if (total <= 0) return 1.0
        val p = aciertos.toDouble() / total
        val sinRedondear = if (p <= 0.60) 1.0 + (p / 0.60) * 3.0
        else 4.0 + ((p - 0.60) / 0.40) * 3.0
        return (sinRedondear * 10).roundToInt() / 10.0
    }

    /** 5,5 en vez de 5.5 */
    fun formatear(nota: Double): String =
        String.format(Locale.forLanguageTag("es-CL"), "%.1f", nota)
}