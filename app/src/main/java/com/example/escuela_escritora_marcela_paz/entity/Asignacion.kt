package com.example.escuela_escritora_marcela_paz.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Lo que define el docente al asignar una unidad autorizada a un curso. */
@Entity(
    tableName = "asignaciones",
    foreignKeys = [ForeignKey(
        entity = Unidad::class,
        parentColumns = ["id"],
        childColumns = ["unidadId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("unidadId"), Index(value = ["unidadId", "curso"], unique = true)]
)
data class Asignacion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val unidadId: Int,
    val docenteId: Int,
    /** Ej.: "2° Básico A" */
    val curso: String,
    /** Medianoche UTC (igual que entrega el DatePicker) */
    val fechaInicio: Long,
    val fechaFin: Long,
    /** Máximo de intentos de la prueba final */
    val maxIntentos: Int,
    val notificarCurso: Boolean = true,
    /** Si el estudiante puede ver qué respondió bien/mal al terminar */
    val permitirVerRespuestas: Boolean = true,
    val creadaEn: Long = System.currentTimeMillis()
)