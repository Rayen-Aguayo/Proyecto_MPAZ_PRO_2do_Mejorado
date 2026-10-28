package com.example.escuela_escritora_marcela_paz.repository

import com.example.escuela_escritora_marcela_paz.data.local.dao.ActividadDao
import com.example.escuela_escritora_marcela_paz.data.local.dao.IntentoDao
import com.example.escuela_escritora_marcela_paz.entity.Actividad
import com.example.escuela_escritora_marcela_paz.entity.ConteoIntentos
import com.example.escuela_escritora_marcela_paz.entity.Intento
import com.example.escuela_escritora_marcela_paz.entity.ResumenActividad
import com.example.escuela_escritora_marcela_paz.util.Fechas
import kotlinx.coroutines.flow.Flow

sealed class ResultadoRespuesta {
    object SinIntentos : ResultadoRespuesta()
    object Vencida : ResultadoRespuesta()
    data class Respondida(val correcta: Boolean, val intentosRestantes: Int) : ResultadoRespuesta()
}

class ActividadRepository(
    private val actividadDao: ActividadDao,
    private val intentoDao: IntentoDao
) {
    val todas: Flow<List<Actividad>> = actividadDao.observarTodas()
    val publicadas: Flow<List<Actividad>> = actividadDao.observarPublicadas()
    val resumen: Flow<List<ResumenActividad>> = intentoDao.observarResumen()

    fun conteos(estudianteId: Int): Flow<List<ConteoIntentos>> =
        intentoDao.observarConteos(estudianteId)

    suspend fun crear(actividad: Actividad) = actividadDao.insertar(actividad)

    suspend fun cambiarPublicada(id: Int, publicada: Boolean) =
        actividadDao.cambiarPublicada(id, publicada)

    suspend fun buscar(id: Int): Actividad? = actividadDao.buscarPorId(id)

    suspend fun responder(
        actividad: Actividad,
        estudianteId: Int,
        elegida: Int
    ): ResultadoRespuesta {
        if (!Fechas.estaVigente(actividad.fechaLimite)) return ResultadoRespuesta.Vencida

        val usados = intentoDao.contar(actividad.id, estudianteId)
        if (usados >= actividad.maxIntentos) return ResultadoRespuesta.SinIntentos

        val correcta = elegida == actividad.respuestaCorrecta
        intentoDao.insertar(
            Intento(
                actividadId = actividad.id,
                estudianteId = estudianteId,
                alternativaElegida = elegida,
                correcta = correcta
            )
        )
        return ResultadoRespuesta.Respondida(correcta, actividad.maxIntentos - usados - 1)
    }
}
