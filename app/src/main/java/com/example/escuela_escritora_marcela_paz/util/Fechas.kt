package com.example.escuela_escritora_marcela_paz.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Fechas {
    private const val DIA_MS = 86_400_000L

    /**
     * El DatePicker de Material entrega la fecha a medianoche UTC.
     * Por eso se formatea en UTC: si no, en Chile se vería un día antes.
     */
    fun formatear(ms: Long): String {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formato.timeZone = TimeZone.getTimeZone("UTC")
        return formato.format(Date(ms))
    }

    /** Fecha y hora real (zona del teléfono), para intentos y observaciones. */
    fun formatearFechaHora(ms: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ms))

    /** Medianoche UTC de hoy (para comparar con lo que entrega el DatePicker). */
    fun inicioHoyUtc(): Long = System.currentTimeMillis() / DIA_MS * DIA_MS

    /** La actividad sigue disponible durante todo el día de su fecha límite. */
    fun estaVigente(fechaLimite: Long): Boolean = inicioHoyUtc() <= fechaLimite

    /** Hoy está dentro del rango de disponibilidad (ambos días incluidos). */
    fun estaEnRango(inicio: Long, fin: Long): Boolean = inicioHoyUtc() in inicio..fin
}