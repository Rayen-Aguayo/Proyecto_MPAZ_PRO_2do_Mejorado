package com.example.escuela_escritora_marcela_paz.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Observación pedagógica del docente sobre el informe de un estudiante en una unidad. */
@Entity(tableName = "observaciones", indices = [Index("estudianteId"), Index("unidadId")])
data class Observacion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val estudianteId: Int,
    val unidadId: Int,
    val docenteId: Int,
    val texto: String,
    val fecha: Long = System.currentTimeMillis()
)