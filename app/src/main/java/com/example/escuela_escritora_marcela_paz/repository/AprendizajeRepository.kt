package com.example.escuela_escritora_marcela_paz.repository

import com.example.escuela_escritora_marcela_paz.data.local.AppDatabase
import com.example.escuela_escritora_marcela_paz.entity.Asignacion
import com.example.escuela_escritora_marcela_paz.entity.AsignacionConUnidad
import com.example.escuela_escritora_marcela_paz.entity.AvanceDesafio
import com.example.escuela_escritora_marcela_paz.entity.AvanceEstudiante
import com.example.escuela_escritora_marcela_paz.entity.ConteoPrueba
import com.example.escuela_escritora_marcela_paz.entity.IntentoPrueba
import com.example.escuela_escritora_marcela_paz.entity.Observacion
import com.example.escuela_escritora_marcela_paz.entity.Pregunta
import com.example.escuela_escritora_marcela_paz.entity.RespuestaPrueba
import com.example.escuela_escritora_marcela_paz.entity.ResumenOA
import com.example.escuela_escritora_marcela_paz.util.Calificacion
import com.example.escuela_escritora_marcela_paz.util.EstadoUnidad
import com.example.escuela_escritora_marcela_paz.util.Fechas
import com.example.escuela_escritora_marcela_paz.util.Validadores
import kotlinx.coroutines.flow.Flow

sealed class ResultadoPrueba {
    object NoDisponible : ResultadoPrueba()
    object SinIntentos : ResultadoPrueba()
    data class Terminada(
        val intentoId: Int,
        val aciertos: Int,
        val total: Int,
        val nota: Double,
        val intentosRestantes: Int
    ) : ResultadoPrueba()
}

/** Una pregunta de la prueba junto a lo que respondió el estudiante. */
data class ItemRevision(val pregunta: Pregunta, val elegida: Int, val correcta: Boolean)

/** Asignaciones, pruebas, avance de desafíos, informes y observaciones. */
class AprendizajeRepository(db: AppDatabase) {

    private val unidadDao = db.unidadDao()
    private val preguntaDao = db.preguntaDao()
    private val asignacionDao = db.asignacionDao()
    private val intentoDao = db.intentoPruebaDao()
    private val observacionDao = db.observacionDao()

    // ---------------- Docente ----------------

    fun asignacionesDocente(docenteId: Int): Flow<List<AsignacionConUnidad>> =
        asignacionDao.observarDeDocente(docenteId)

    suspend fun asignar(
        unidadId: Int,
        docenteId: Int,
        curso: String,
        inicio: Long,
        fin: Long,
        maxIntentos: Int,
        notificar: Boolean,
        permitirVerRespuestas: Boolean
    ): String? {
        val unidad = unidadDao.buscarPorId(unidadId) ?: return "La unidad ya no existe."
        if (unidad.estadoUnidad() != EstadoUnidad.AUTORIZADA) {
            return "Solo se pueden asignar unidades autorizadas por UTP."
        }
        if (asignacionDao.existe(unidadId, curso) > 0) {
            return "Esta unidad ya está asignada a $curso."
        }
        asignacionDao.insertar(
            Asignacion(
                unidadId = unidadId,
                docenteId = docenteId,
                curso = curso,
                fechaInicio = inicio,
                fechaFin = fin,
                maxIntentos = maxIntentos,
                notificarCurso = notificar,
                permitirVerRespuestas = permitirVerRespuestas
            )
        )
        return null
    }

    fun avanceCurso(asignacionId: Int, curso: String): Flow<List<AvanceEstudiante>> =
        intentoDao.observarAvanceCurso(asignacionId, curso)

    fun resumenPorOA(docenteId: Int, curso: String?): Flow<List<ResumenOA>> =
        intentoDao.observarResumenPorOA(docenteId, curso)

    fun intentos(asignacionId: Int, estudianteId: Int): Flow<List<IntentoPrueba>> =
        intentoDao.observarIntentos(asignacionId, estudianteId)

    fun observaciones(estudianteId: Int, unidadId: Int): Flow<List<Observacion>> =
        observacionDao.observarDe(estudianteId, unidadId)

    suspend fun agregarObservacion(
        docenteId: Int,
        estudianteId: Int,
        unidadId: Int,
        texto: String
    ): String? {
        Validadores.observacion(texto)?.let { return it }
        observacionDao.insertar(
            Observacion(
                estudianteId = estudianteId,
                unidadId = unidadId,
                docenteId = docenteId,
                texto = texto.trim()
            )
        )
        return null
    }

    /** Respuestas de un intento, pregunta por pregunta (correctas e incorrectas). */
    suspend fun revision(unidadId: Int, intentoId: Int): List<ItemRevision> {
        val respuestas = intentoDao.respuestasDe(intentoId)
        val porId = preguntaDao.pruebaFinalDe(unidadId).associateBy { it.id }
        return respuestas.mapNotNull { r ->
            porId[r.preguntaId]?.let { ItemRevision(it, r.elegida, r.correcta) }
        }
    }

    // ---------------- Estudiante ----------------

    fun vigentesDeCurso(curso: String): Flow<List<AsignacionConUnidad>> =
        asignacionDao.observarVigentesDeCurso(curso, Fechas.inicioHoyUtc())

    fun conteos(estudianteId: Int): Flow<List<ConteoPrueba>> =
        intentoDao.observarConteos(estudianteId)

    fun avance(estudianteId: Int, unidadId: Int): Flow<List<AvanceDesafio>> =
        preguntaDao.observarAvance(estudianteId, unidadId)

    suspend fun marcarDesafio(estudianteId: Int, pregunta: Pregunta) {
        preguntaDao.registrarAvance(
            AvanceDesafio(
                estudianteId = estudianteId,
                preguntaId = pregunta.id,
                unidadId = pregunta.unidadId
            )
        )
    }

    /** Corrige la prueba final, calcula la nota y guarda el intento con sus respuestas. */
    suspend fun rendirPrueba(
        asignacion: Asignacion,
        estudianteId: Int,
        respuestas: Map<Int, Int>
    ): ResultadoPrueba {
        if (!Fechas.estaEnRango(asignacion.fechaInicio, asignacion.fechaFin)) {
            return ResultadoPrueba.NoDisponible
        }
        val unidad = unidadDao.buscarPorId(asignacion.unidadId)
        if (unidad == null || unidad.estadoUnidad() != EstadoUnidad.AUTORIZADA) {
            return ResultadoPrueba.NoDisponible
        }
        val usados = intentoDao.contar(asignacion.id, estudianteId)
        if (usados >= asignacion.maxIntentos) return ResultadoPrueba.SinIntentos

        val preguntas = preguntaDao.pruebaFinalDe(asignacion.unidadId)
        if (preguntas.isEmpty()) return ResultadoPrueba.NoDisponible

        val detalle = preguntas.map { p ->
            val elegida = respuestas[p.id] ?: -1
            RespuestaPrueba(
                intentoId = 0,
                preguntaId = p.id,
                elegida = elegida,
                correcta = elegida == p.correcta
            )
        }
        val aciertos = detalle.count { it.correcta }
        val nota = Calificacion.nota(aciertos, preguntas.size)
        val intentoId = intentoDao.registrar(
            IntentoPrueba(
                asignacionId = asignacion.id,
                estudianteId = estudianteId,
                aciertos = aciertos,
                total = preguntas.size,
                nota = nota
            ),
            detalle
        ).toInt()

        return ResultadoPrueba.Terminada(
            intentoId = intentoId,
            aciertos = aciertos,
            total = preguntas.size,
            nota = nota,
            intentosRestantes = asignacion.maxIntentos - usados - 1
        )
    }
}
