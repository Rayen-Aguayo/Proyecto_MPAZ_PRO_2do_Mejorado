package com.example.escuela_escritora_marcela_paz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.escuela_escritora_marcela_paz.entity.Observacion
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservacionDao {
    @Insert
    suspend fun insertar(observacion: Observacion)

    @Query("SELECT * FROM observaciones WHERE estudianteId = :estudianteId AND unidadId = :unidadId ORDER BY fecha DESC")
    fun observarDe(estudianteId: Int, unidadId: Int): Flow<List<Observacion>>

    @Query("SELECT * FROM observaciones WHERE unidadId = :unidadId ORDER BY fecha DESC")
    fun observarDeUnidad(unidadId: Int): Flow<List<Observacion>>
}
