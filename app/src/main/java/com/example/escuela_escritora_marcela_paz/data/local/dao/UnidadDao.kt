package com.example.escuela_escritora_marcela_paz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.escuela_escritora_marcela_paz.entity.Unidad
import kotlinx.coroutines.flow.Flow

@Dao
interface UnidadDao {
    @Insert
    suspend fun insertar(unidad: Unidad): Long

    @Update
    suspend fun actualizar(unidad: Unidad)

    @Query("SELECT * FROM unidades ORDER BY creadaEn DESC")
    fun observarTodas(): Flow<List<Unidad>>

    /** Lo que el docente puede consultar y asignar: solo unidades autorizadas por UTP. */
    @Query("SELECT * FROM unidades WHERE estado = 'AUTORIZADA' ORDER BY asignatura, codigoOA")
    fun observarAutorizadas(): Flow<List<Unidad>>

    @Query("SELECT * FROM unidades WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Unidad?

    @Query("UPDATE unidades SET estado = :estado WHERE id = :id")
    suspend fun cambiarEstado(id: Int, estado: String)
}