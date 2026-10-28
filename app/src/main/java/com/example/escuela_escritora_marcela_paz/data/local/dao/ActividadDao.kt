package com.example.escuela_escritora_marcela_paz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.escuela_escritora_marcela_paz.entity.Actividad
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {
    @Insert
    suspend fun insertar(actividad: Actividad)

    @Query("SELECT * FROM actividades ORDER BY creadaEn DESC")
    fun observarTodas(): Flow<List<Actividad>>

    @Query("SELECT * FROM actividades WHERE publicada = 1 ORDER BY creadaEn DESC")
    fun observarPublicadas(): Flow<List<Actividad>>

    @Query("SELECT * FROM actividades WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Actividad?

    @Query("UPDATE actividades SET publicada = :publicada WHERE id = :id")
    suspend fun cambiarPublicada(id: Int, publicada: Boolean)
}
