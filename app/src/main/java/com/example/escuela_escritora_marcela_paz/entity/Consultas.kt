package com.example.escuela_escritora_marcela_paz.entity

import androidx.room.Embedded
import androidx.room.Relation

/** Una asignación junto a su unidad (lo que ve el estudiante y el docente). */
data class AsignacionConUnidad(
    @Embedded val asignacion: Asignacion,
    @Relation(parentColumn = "unidadId", entityColumn = "id")
    val unidad: Unidad
)

/** Rendimiento agrupado por objetivo de aprendizaje (informe del docente). */
data class ResumenOA(
    val codigoOA: String,
    val objetivo: String,
    val asignatura: String,
    val estudiantes: Int,
    val intentos: Int,
    val promedio: Double
)

/** Seguimiento de un estudiante en una asignación (monitoreo del docente). */
data class AvanceEstudiante(
    val estudianteId: Int,
    val nombre: String,
    val intentos: Int,
    val mejorNota: Double
)

/** Intentos usados por asignación para un estudiante. */
data class ConteoPrueba(
    val asignacionId: Int,
    val total: Int
)