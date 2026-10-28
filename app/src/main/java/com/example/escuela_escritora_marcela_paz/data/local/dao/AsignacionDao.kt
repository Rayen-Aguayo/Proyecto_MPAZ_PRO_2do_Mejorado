package com.example.escuela_escritora_marcela_paz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.escuela_escritora_marcela_paz.entity.Asignacion
import com.example.escuela_escritora_marcela_paz.entity.AsignacionConUnidad
import kotlinx.coroutines.flow.Flow

@Dao
interface AsignacionDao {
    @Insert
    suspend fun insertar(asignacion: Asignacion): Long

    @Query("SELECT COUNT(*) FROM asignaciones WHERE unidadId = :unidadId AND curso = :curso")
    suspend fun existe(unidadId: Int, curso: String): Int

    @Query("SELECT * FROM asignaciones WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Asignacion?

    /** Todas las asignaciones hechas por un docente. */
    @Transaction
    @Query("SELECT * FROM asignaciones WHERE docenteId = :docenteId ORDER BY creadaEn DESC")
    fun observarDeDocente(docenteId: Int): Flow<List<AsignacionConUnidad>>

    /**
     * Lo que ve un estudiante: solo lo asignado a su curso, con la unidad aún autorizada.
     * "hoy" es medianoche UTC (Fechas.inicioHoyUtc()), el rango incluye ambos días.
     */
    @Transaction
    @Query(
        "SELECT a.* FROM asignaciones a JOIN unidades u ON u.id = a.unidadId " +
                "WHERE a.curso = :curso AND u.estado = 'AUTORIZADA' " +
                "AND :hoy BETWEEN a.fechaInicio AND a.fechaFin " +
                "ORDER BY u.asignatura, u.codigoOA"
    )
    fun observarVigentesDeCurso(curso: String, hoy: Long): Flow<List<AsignacionConUnidad>>
}
