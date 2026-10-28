package com.example.escuela_escritora_marcela_paz.repository

import com.example.escuela_escritora_marcela_paz.data.local.dao.PreguntaDao
import com.example.escuela_escritora_marcela_paz.data.local.dao.UnidadDao
import com.example.escuela_escritora_marcela_paz.entity.Pregunta
import com.example.escuela_escritora_marcela_paz.entity.Unidad
import com.example.escuela_escritora_marcela_paz.util.EstadoUnidad
import kotlinx.coroutines.flow.Flow

/**
 * Reglas de la UTP. Las funciones que modifican devuelven null si salió bien,
 * o el motivo por el que no se pudo (la pantalla lo muestra).
 */
class UnidadRepository(
    private val unidadDao: UnidadDao,
    private val preguntaDao: PreguntaDao
) {
    val todas: Flow<List<Unidad>> = unidadDao.observarTodas()
    val autorizadas: Flow<List<Unidad>> = unidadDao.observarAutorizadas()

    fun preguntasDe(unidadId: Int): Flow<List<Pregunta>> = preguntaDao.observarDeUnidad(unidadId)

    suspend fun buscar(id: Int): Unidad? = unidadDao.buscarPorId(id)

    /** Crea (id = 0) o edita una unidad. Una unidad autorizada no se edita. */
    suspend fun guardar(unidad: Unidad): String? {
        if (unidad.id == 0) {
            unidadDao.insertar(unidad)
            return null
        }
        val actual = unidadDao.buscarPorId(unidad.id) ?: return "La unidad ya no existe."
        if (!actual.estadoUnidad().sePuedeEditar()) {
            return "Una unidad autorizada no se puede editar. Deshabilítala primero."
        }
        unidadDao.actualizar(
            unidad.copy(
                estado = actual.estado,
                creadorId = actual.creadorId,
                creadaEn = actual.creadaEn
            )
        )
        return null
    }

    suspend fun cambiarEstado(id: Int, nuevo: EstadoUnidad): String? {
        val unidad = unidadDao.buscarPorId(id) ?: return "La unidad ya no existe."
        val actual = unidad.estadoUnidad()

        if (nuevo !in actual.siguientesPermitidos()) {
            return "No se puede pasar de ${actual.texto} a ${nuevo.texto}."
        }
        if (nuevo == EstadoUnidad.EN_REVISION) {
            if (preguntaDao.contar(id, false) == 0) {
                return "Agrega al menos un desafío antes de enviar la unidad a revisión."
            }
            if (preguntaDao.contar(id, true) == 0) {
                return "Agrega al menos una pregunta de prueba final antes de enviar a revisión."
            }
        }
        unidadDao.cambiarEstado(id, nuevo.name)
        return null
    }

    suspend fun agregarPregunta(pregunta: Pregunta): String? {
        val unidad = unidadDao.buscarPorId(pregunta.unidadId) ?: return "La unidad ya no existe."
        if (!unidad.estadoUnidad().sePuedeEditar()) {
            return "Una unidad autorizada no admite cambios en sus preguntas."
        }
        val orden = preguntaDao.siguienteOrden(pregunta.unidadId, pregunta.esPruebaFinal)
        preguntaDao.insertar(pregunta.copy(orden = orden))
        return null
    }

    suspend fun eliminarPregunta(id: Int): String? {
        val pregunta = preguntaDao.buscarPorId(id) ?: return "La pregunta ya no existe."
        val unidad = unidadDao.buscarPorId(pregunta.unidadId) ?: return "La unidad ya no existe."
        if (!unidad.estadoUnidad().sePuedeEditar()) {
            return "Una unidad autorizada no admite cambios en sus preguntas."
        }
        preguntaDao.eliminar(id)
        return null
    }
}