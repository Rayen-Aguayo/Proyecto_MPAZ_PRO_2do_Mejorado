package com.example.escuela_escritora_marcela_paz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.escuela_escritora_marcela_paz.data.local.AppDatabase
import com.example.escuela_escritora_marcela_paz.entity.Asignacion
import com.example.escuela_escritora_marcela_paz.entity.AsignacionConUnidad
import com.example.escuela_escritora_marcela_paz.entity.AvanceDesafio
import com.example.escuela_escritora_marcela_paz.entity.IntentoPrueba
import com.example.escuela_escritora_marcela_paz.entity.Pregunta
import com.example.escuela_escritora_marcela_paz.entity.Usuario
import com.example.escuela_escritora_marcela_paz.repository.AprendizajeRepository
import com.example.escuela_escritora_marcela_paz.repository.ItemRevision
import com.example.escuela_escritora_marcela_paz.repository.ResultadoPrueba
import com.example.escuela_escritora_marcela_paz.repository.UnidadRepository
import com.example.escuela_escritora_marcela_paz.util.Calificacion
import com.example.escuela_escritora_marcela_paz.util.Notificaciones
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel del perfil estudiante: solo ve lo que su docente asignó a su curso. */
@OptIn(ExperimentalCoroutinesApi::class)
class EstudianteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val unidadRepo = UnidadRepository(db.unidadDao(), db.preguntaDao())
    private val repo = AprendizajeRepository(db)

    private val _usuario = MutableStateFlow<Usuario?>(null)

    fun iniciar(usuario: Usuario) {
        _usuario.value = usuario
    }

    /** Asignaciones vigentes del curso del estudiante, con su unidad autorizada. */
    val asignaciones: StateFlow<List<AsignacionConUnidad>> = _usuario
        .flatMapLatest { u ->
            val curso = u?.curso
            if (curso == null) flowOf(emptyList()) else repo.vigentesDeCurso(curso)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** asignacionId -> intentos de prueba final usados */
    val conteos: StateFlow<Map<Int, Int>> = _usuario
        .flatMapLatest { u -> if (u == null) flowOf(emptyList()) else repo.conteos(u.id) }
        .map { lista -> lista.associate { it.asignacionId to it.total } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun preguntas(unidadId: Int): Flow<List<Pregunta>> = unidadRepo.preguntasDe(unidadId)

    // ---------- Desafíos ----------
    fun avance(unidadId: Int): Flow<List<AvanceDesafio>> = _usuario.flatMapLatest { u ->
        if (u == null) flowOf(emptyList()) else repo.avance(u.id, unidadId)
    }

    fun marcarDesafio(pregunta: Pregunta) {
        val u = _usuario.value ?: return
        viewModelScope.launch { repo.marcarDesafio(u.id, pregunta) }
    }

    // ---------- Prueba final ----------
    private val _resultado = MutableStateFlow<ResultadoPrueba?>(null)
    val resultado: StateFlow<ResultadoPrueba?> = _resultado.asStateFlow()

    private val _revision = MutableStateFlow<List<ItemRevision>>(emptyList())
    val revision: StateFlow<List<ItemRevision>> = _revision.asStateFlow()

    private val _ultimoIntento = MutableStateFlow<IntentoPrueba?>(null)
    val ultimoIntento: StateFlow<IntentoPrueba?> = _ultimoIntento.asStateFlow()

    fun limpiarPrueba() {
        _resultado.value = null
        _revision.value = emptyList()
        _ultimoIntento.value = null
    }

    fun rendir(asignacion: Asignacion, respuestas: Map<Int, Int>) {
        val u = _usuario.value ?: return
        viewModelScope.launch {
            val r = repo.rendirPrueba(asignacion, u.id, respuestas)
            if (r is ResultadoPrueba.Terminada) {
                if (asignacion.permitirVerRespuestas) {
                    _revision.value = repo.revision(asignacion.unidadId, r.intentoId)
                }
                Notificaciones.mostrar(
                    getApplication(),
                    "Prueba terminada",
                    "Tu nota fue ${Calificacion.formatear(r.nota)}"
                )
            }
            _resultado.value = r
        }
    }

    /** Para cuando ya no quedan intentos: muestra el último resultado y su revisión. */
    fun cargarUltimoIntento(asignacion: Asignacion) {
        val u = _usuario.value ?: return
        viewModelScope.launch {
            val ultimo = repo.intentos(asignacion.id, u.id).first().firstOrNull() ?: return@launch
            _ultimoIntento.value = ultimo
            if (asignacion.permitirVerRespuestas) {
                _revision.value = repo.revision(asignacion.unidadId, ultimo.id)
            }
        }
    }
}
