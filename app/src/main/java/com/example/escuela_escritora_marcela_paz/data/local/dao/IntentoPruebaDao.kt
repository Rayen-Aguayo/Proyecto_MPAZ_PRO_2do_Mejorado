package com.example.escuela_escritora_marcela_paz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.escuela_escritora_marcela_paz.entity.AvanceEstudiante
import com.example.escuela_escritora_marcela_paz.entity.ConteoPrueba
import com.example.escuela_escritora_marcela_paz.entity.IntentoPrueba
import com.example.escuela_escritora_marcela_paz.entity.RespuestaPrueba
import com.example.escuela_escritora_marcela_paz.entity.ResumenOA
import kotlinx.coroutines.flow.Flow

@Dao
abstract class IntentoPruebaDao {
    @Insert
    abstract suspend fun insertarIntento(intento: IntentoPrueba): Long

    @Insert
    abstract suspend fun insertarRespuestas(respuestas: List<RespuestaPrueba>)

    /** Guarda el intento y sus respuestas juntos: o se guarda todo o nada. */
    @Transaction
    open suspend fun registrar(intento: IntentoPrueba, respuestas: List<RespuestaPrueba>): Long {
        val id = insertarIntento(intento)
        insertarRespuestas(respuestas.map { it.copy(intentoId = id.toInt()) })
        return id
    }

    @Query("SELECT COUNT(*) FROM intentos_prueba WHERE asignacionId = :asignacionId AND estudianteId = :estudianteId")
    abstract suspend fun contar(asignacionId: Int, estudianteId: Int): Int

    @Query(
        "SELECT asignacionId AS asignacionId, COUNT(*) AS total " +
                "FROM intentos_prueba WHERE estudianteId = :estudianteId GROUP BY asignacionId"
    )
    abstract fun observarConteos(estudianteId: Int): Flow<List<ConteoPrueba>>

    @Query("SELECT * FROM intentos_prueba WHERE asignacionId = :asignacionId AND estudianteId = :estudianteId ORDER BY fecha DESC")
    abstract fun observarIntentos(asignacionId: Int, estudianteId: Int): Flow<List<IntentoPrueba>>

    @Query("SELECT * FROM respuestas_prueba WHERE intentoId = :intentoId ORDER BY id")
    abstract suspend fun respuestasDe(intentoId: Int): List<RespuestaPrueba>

    /** Monitoreo: cada estudiante del curso con sus intentos y mejor nota en una asignación. */
    @Query(
        "SELECT us.id AS estudianteId, us.nombre AS nombre, COUNT(ip.id) AS intentos, " +
                "COALESCE(MAX(ip.nota), 0.0) AS mejorNota " +
                "FROM usuarios us LEFT JOIN intentos_prueba ip " +
                "ON ip.estudianteId = us.id AND ip.asignacionId = :asignacionId " +
                "WHERE us.rol = 'ESTUDIANTE' AND us.curso = :curso " +
                "GROUP BY us.id, us.nombre ORDER BY us.nombre"
    )
    abstract fun observarAvanceCurso(asignacionId: Int, curso: String): Flow<List<AvanceEstudiante>>

    /** Rendimiento agrupado por objetivo de aprendizaje (curso = null → todos los cursos). */
    @Query(
        "SELECT u.codigoOA AS codigoOA, u.objetivo AS objetivo, u.asignatura AS asignatura, " +
                "COUNT(DISTINCT ip.estudianteId) AS estudiantes, COUNT(ip.id) AS intentos, " +
                "COALESCE(AVG(ip.nota), 0.0) AS promedio " +
                "FROM unidades u JOIN asignaciones a ON a.unidadId = u.id " +
                "LEFT JOIN intentos_prueba ip ON ip.asignacionId = a.id " +
                "WHERE a.docenteId = :docenteId AND (:curso IS NULL OR a.curso = :curso) " +
                "GROUP BY u.codigoOA, u.objetivo, u.asignatura ORDER BY u.asignatura, u.codigoOA"
    )
    abstract fun observarResumenPorOA(docenteId: Int, curso: String?): Flow<List<ResumenOA>>
}