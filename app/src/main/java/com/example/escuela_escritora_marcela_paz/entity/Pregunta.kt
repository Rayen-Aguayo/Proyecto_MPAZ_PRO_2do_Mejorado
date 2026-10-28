package com.example.escuela_escritora_marcela_paz.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Pregunta de una unidad. Si esPruebaFinal = false es un desafío de práctica
 * (el estudiante los hace en orden, de menor a mayor dificultad).
 */
@Entity(
    tableName = "preguntas",
    foreignKeys = [ForeignKey(
        entity = Unidad::class,
        parentColumns = ["id"],
        childColumns = ["unidadId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("unidadId")]
)
data class Pregunta(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val unidadId: Int,
    val texto: String,
    val alternativaA: String,
    val alternativaB: String,
    val alternativaC: String,
    val alternativaD: String,
    /** 0 = A, 1 = B, 2 = C, 3 = D */
    val correcta: Int,
    val esPruebaFinal: Boolean,
    /** Posición dentro de los desafíos / de la prueba */
    val orden: Int = 0
) {
    fun alternativas(): List<String> =
        listOf(alternativaA, alternativaB, alternativaC, alternativaD)
}
