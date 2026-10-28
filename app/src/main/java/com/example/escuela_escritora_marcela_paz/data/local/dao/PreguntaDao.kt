package com.example.escuela_escritora_marcela_paz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.escuela_escritora_marcela_paz.entity.AvanceDesafio
import com.example.escuela_escritora_marcela_paz.entity.Pregunta
import kotlinx.coroutines.flow.Flow

@Dao
interface PreguntaDao {
    @Insert
    suspend fun insertar(pregunta: Pregunta): Long

    @Query("DELETE FROM preguntas WHERE id = :id")
    suspend fun eliminar(id: Int)

    @Query("SELECT * FROM preguntas WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Pregunta?

    @Query("SELECT * FROM preguntas WHERE unidadId = :unidadId ORDER BY esPruebaFinal, orden, id")
    fun observarDeUnidad(unidadId: Int): Flow<List<Pregunta>>

    @Query("SELECT * FROM preguntas WHERE unidadId = :unidadId AND esPruebaFinal = 1 ORDER BY orden, id")
    suspend fun pruebaFinalDe(unidadId: Int): List<Pregunta>

    @Query("SELECT COUNT(*) FROM preguntas WHERE unidadId = :unidadId AND esPruebaFinal = :esPruebaFinal")
    suspend fun contar(unidadId: Int, esPruebaFinal: Boolean): Int

    @Query("SELECT COALESCE(MAX(orden), 0) + 1 FROM preguntas WHERE unidadId = :unidadId AND esPruebaFinal = :esPruebaFinal")
    suspend fun siguienteOrden(unidadId: Int, esPruebaFinal: Boolean): Int

    // ----- avance en desafíos -----
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registrarAvance(avance: AvanceDesafio)

    @Query("SELECT * FROM avance_desafios WHERE estudianteId = :estudianteId AND unidadId = :unidadId")
    fun observarAvance(estudianteId: Int, unidadId: Int): Flow<List<AvanceDesafio>>
}