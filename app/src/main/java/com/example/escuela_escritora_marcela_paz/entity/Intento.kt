package com.example.escuela_escritora_marcela_paz.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "intentos",
    indices = [Index("actividadId"), Index("estudianteId")]
)
data class Intento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val actividadId: Int,
    val estudianteId: Int,
    val alternativaElegida: Int,
    val correcta: Boolean,
    val fecha: Long = System.currentTimeMillis()
)

/** Resultado de consulta: resumen por actividad para el docente. */
data class ResumenActividad(
    val titulo: String,
    val asignatura: String,
    val total: Int,
    val aciertos: Int
)

/** Resultado de consulta: intentos usados por actividad para un estudiante. */
data class ConteoIntentos(
    val actividadId: Int,
    val total: Int
)
