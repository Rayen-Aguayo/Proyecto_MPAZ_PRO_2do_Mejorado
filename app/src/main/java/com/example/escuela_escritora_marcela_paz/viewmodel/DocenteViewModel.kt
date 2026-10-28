package com.example.escuela_escritora_marcela_paz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.escuela_escritora_marcela_paz.data.local.AppDatabase
import com.example.escuela_escritora_marcela_paz.entity.AsignacionConUnidad
import com.example.escuela_escritora_marcela_paz.entity.AvanceEstudiante
import com.example.escuela_escritora_marcela_paz.entity.IntentoPrueba
import com.example.escuela_escritora_marcela_paz.entity.Observacion
import com.example.escuela_escritora_marcela_paz.entity.ResumenOA
import com.example.escuela_escritora_marcela_paz.entity.Unidad
import com.example.escuela_escritora_marcela_paz.repository.AprendizajeRepository
import com.example.escuela_escritora_marcela_paz.repository.ItemRevision
import com.example.escuela_escritora_marcela_paz.repository.UnidadRepository
import com.example.escuela_escritora_marcela_paz.util.Validadores
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AsignacionErrores(
    val curso: String? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val intentos: String? = null,
    val general: String? = null
) {
    fun hayErrores() = listOf(curso, fechaInicio, fechaFin, intentos, general).any { it != null }
}

/** ViewModel del perfil docente: unidades autorizadas, asignaciones, monitoreo e informes. */
@OptIn(ExperimentalCoroutinesApi::class)
class DocenteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val unidadRepo = UnidadRepository(db.unidadDao(), db.preguntaDao())
    private val repo = AprendizajeRepository(db)

    private val _docenteId = MutableStateFlow<Int?>(null)

    fun iniciar(docenteId: Int) {
        _docenteId.value = docenteId
    }

    /** Solo las unidades que la UTP autorizó. */
    val autorizadas: StateFlow<List<Unidad>> = unidadRepo.autorizadas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val asignaciones: StateFlow<List<AsignacionConUnidad>> = _docenteId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repo.asignacionesDocente(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- Rendimiento agrupado por objetivo de aprendizaje ----------
    private val _cursoFiltro = MutableStateFlow<String?>(null)
    val cursoFiltro: StateFlow<String?> = _cursoFiltro.asStateFlow()

    fun filtrarCurso(curso: String?) {
        _cursoFiltro.value = curso
    }

    val resumenOA: StateFlow<List<ResumenOA>> = combine(_docenteId, _cursoFiltro) { id, curso ->
        id to curso
    }.flatMapLatest { (id, curso) ->
        if (id == null) flowOf(emptyList()) else repo.resumenPorOA(id, curso)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- Asignar una unidad a un curso ----------
    private val _errores = MutableStateFlow(AsignacionErrores())
    val errores: StateFlow<AsignacionErrores> = _errores.asStateFlow()

    private val _asignadoOk = MutableStateFlow(false)
    val asignadoOk: StateFlow<Boolean> = _asignadoOk.asStateFlow()

    fun nuevaAsignacion() {
        _errores.value = AsignacionErrores()
        _asignadoOk.value = false
    }

    fun consumirAsignado() {
        _asignadoOk.value = false
    }

    fun asignar(
        unidadId: Int,
        curso: String,
        inicio: Long?,
        fin: Long?,
        intentos: String,
        notificar: Boolean,
        permitirVerRespuestas: Boolean
    ) {
        val docenteId = _docenteId.value ?: return
        val errores = AsignacionErrores(
            curso = Validadores.curso(curso),
            fechaInicio = Validadores.fechaInicio(inicio),
            fechaFin = Validadores.fechaFin(inicio, fin),
            intentos = Validadores.intentosPrueba(intentos)
        )
        _errores.value = errores
        if (errores.hayErrores()) return

        viewModelScope.launch {
            try {
                val error = repo.asignar(
                    unidadId = unidadId,
                    docenteId = docenteId,
                    curso = curso,
                    inicio = inicio!!,
                    fin = fin!!,
                    maxIntentos = intentos.toInt(),
                    notificar = notificar,
                    permitirVerRespuestas = permitirVerRespuestas
                )
                if (error == null) _asignadoOk.value = true
                else _errores.value = errores.copy(general = error)
            } catch (e: Exception) {
                _errores.value = errores.copy(general = "No se pudo asignar la unidad")
            }
        }
    }

    // ---------- Monitoreo e informe por estudiante ----------
    fun avanceCurso(asignacionId: Int, curso: String): Flow<List<AvanceEstudiante>> =
        repo.avanceCurso(asignacionId, curso)

    fun intentos(asignacionId: Int, estudianteId: Int): Flow<List<IntentoPrueba>> =
        repo.intentos(asignacionId, estudianteId)

    suspend fun revision(unidadId: Int, intentoId: Int): List<ItemRevision> =
        repo.revision(unidadId, intentoId)

    fun observaciones(estudianteId: Int, unidadId: Int): Flow<List<Observacion>> =
        repo.observaciones(estudianteId, unidadId)

    private val _nombreEstudiante = MutableStateFlow("")
    val nombreEstudiante: StateFlow<String> = _nombreEstudiante.asStateFlow()

    fun cargarEstudiante(estudianteId: Int) {
        viewModelScope.launch {
            _nombreEstudiante.value = db.usuarioDao().buscarPorId(estudianteId)?.nombre ?: ""
        }
    }

    /** alTerminar recibe null si se guardó, o el motivo del error. */
    fun agregarObservacion(
        estudianteId: Int,
        unidadId: Int,
        texto: String,
        alTerminar: (String?) -> Unit
    ) {
        val docenteId = _docenteId.value ?: return
        viewModelScope.launch {
            val error = try {
                repo.agregarObservacion(docenteId, estudianteId, unidadId, texto)
            } catch (e: Exception) {
                "No se pudo guardar la observación"
            }
            alTerminar(error)
        }
    }
}