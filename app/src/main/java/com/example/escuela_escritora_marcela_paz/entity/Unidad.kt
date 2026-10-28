package com.example.escuela_escritora_marcela_paz.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.escuela_escritora_marcela_paz.util.EstadoUnidad

/** Unidad didáctica que crea la UTP para una asignatura y un objetivo de aprendizaje (OA). */
@Entity(tableName = "unidades")
data class Unidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val asignatura: String,
    val titulo: String,
    /** Ej.: "OA 3" */
    val codigoOA: String,
    val objetivo: String,
    /** Texto breve que lee el estudiante */
    val explicacion: String,
    val ejemplo: String,
    /** Texto de las instrucciones; se reproduce en voz alta (TextToSpeech) */
    val instrucciones: String = "",
    /** Imagen elegida desde la galería (uri en texto) */
    val imagenUri: String? = null,
    /** Nombre de EstadoUnidad */
    val estado: String = EstadoUnidad.BORRADOR.name,
    val creadorId: Int,
    val creadaEn: Long = System.currentTimeMillis()
) {
    fun estadoUnidad(): EstadoUnidad = EstadoUnidad.desde(estado)
}
