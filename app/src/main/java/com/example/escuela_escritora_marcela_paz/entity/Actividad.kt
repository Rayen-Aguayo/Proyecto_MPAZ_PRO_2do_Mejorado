package com.example.escuela_escritora_marcela_paz.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "actividades")
data class Actividad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val asignatura: String,
    val pregunta: String,
    val alternativaA: String,
    val alternativaB: String,
    val alternativaC: String,
    /** 0 = A, 1 = B, 2 = C */
    val respuestaCorrecta: Int,
    val maxIntentos: Int,
    /** Fecha límite en milisegundos (medianoche UTC del día elegido) */
    val fechaLimite: Long,
    val conAudio: Boolean,
    val publicada: Boolean,
    val creadorId: Int,
    val creadaEn: Long = System.currentTimeMillis()
) {
    fun alternativas(): List<String> = listOf(alternativaA, alternativaB, alternativaC)
}
