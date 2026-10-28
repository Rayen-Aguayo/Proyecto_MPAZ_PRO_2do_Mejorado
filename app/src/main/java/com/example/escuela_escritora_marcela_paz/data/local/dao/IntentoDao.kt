package com.example.escuela_escritora_marcela_paz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.escuela_escritora_marcela_paz.entity.ConteoIntentos
import com.example.escuela_escritora_marcela_paz.entity.Intento
import com.example.escuela_escritora_marcela_paz.entity.ResumenActividad
import kotlinx.coroutines.flow.Flow

@Dao
interface IntentoDao {
    @Insert
    suspend fun insertar(intento: Intento)

    @Query("SELECT COUNT(*) FROM intentos WHERE actividadId = :actividadId AND estudianteId = :estudianteId")
    suspend fun contar(actividadId: Int, estudianteId: Int): Int

    @Query(
        "SELECT actividadId AS actividadId, COUNT(*) AS total " +
            "FROM intentos WHERE estudianteId = :estudianteId GROUP BY actividadId"
    )
    fun observarConteos(estudianteId: Int): Flow<List<ConteoIntentos>>

    @Query(
        "SELECT a.titulo AS titulo, a.asignatura AS asignatura, " +
            "COUNT(i.id) AS total, COALESCE(SUM(i.correcta), 0) AS aciertos " +
            "FROM actividades a LEFT JOIN intentos i ON i.actividadId = a.id " +
            "GROUP BY a.id ORDER BY a.creadaEn DESC"
    )
    fun observarResumen(): Flow<List<ResumenActividad>>
}
