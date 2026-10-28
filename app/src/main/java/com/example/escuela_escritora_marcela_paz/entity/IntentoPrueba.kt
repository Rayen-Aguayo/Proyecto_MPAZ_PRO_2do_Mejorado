package com.example.escuela_escritora_marcela_paz.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Un intento completo de la prueba final de una unidad asignada. */
@Entity(
    tableName = "intentos_prueba",
    foreignKeys = [ForeignKey(
        entity = Asignacion::class,
        parentColumns = ["id"],
        childColumns = ["asignacionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("asignacionId"), Index("estudianteId")]
)
data class IntentoPrueba(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val asignacionId: Int,
    val estudianteId: Int,
    val aciertos: Int,
    val total: Int,
    /** Escala 1,0 a 7,0 */
    val nota: Double,
    val fecha: Long = System.currentTimeMillis()
)

/** Qué eligió el estudiante en cada pregunta de un intento (para revisar correctas/incorrectas). */
@Entity(
    tableName = "respuestas_prueba",
    foreignKeys = [ForeignKey(
        entity = IntentoPrueba::class,
        parentColumns = ["id"],
        childColumns = ["intentoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("intentoId")]
)
data class RespuestaPrueba(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val intentoId: Int,
    val preguntaId: Int,
    val elegida: Int,
    val correcta: Boolean
)

/** Desafío de práctica ya resuelto por un estudiante (confirmación de avance). */
@Entity(tableName = "avance_desafios", primaryKeys = ["estudianteId", "preguntaId"])
data class AvanceDesafio(
    val estudianteId: Int,
    val preguntaId: Int,
    val unidadId: Int,
    val fecha: Long = System.currentTimeMillis()
)
