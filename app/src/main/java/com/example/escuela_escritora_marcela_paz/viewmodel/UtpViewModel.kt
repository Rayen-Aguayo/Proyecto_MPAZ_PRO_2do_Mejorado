package com.example.escuela_escritora_marcela_paz.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.escuela_escritora_marcela_paz.data.local.AppDatabase
import com.example.escuela_escritora_marcela_paz.entity.Pregunta
import com.example.escuela_escritora_marcela_paz.entity.Unidad
import com.example.escuela_escritora_marcela_paz.repository.UnidadRepository
import com.example.escuela_escritora_marcela_paz.util.EstadoUnidad
import com.example.escuela_escritora_marcela_paz.util.Imagenes
import com.example.escuela_escritora_marcela_paz.util.Validadores
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UnidadForm(
    val asignatura: String = "",
    val titulo: String = "",
    val codigoOA: String = "",
    val objetivo: String = "",
    val explicacion: String = "",
    val ejemplo: String = "",
    val instrucciones: String = "",
    val imagenUri: String? = null
)

data class UnidadErrores(
    val asignatura: String? = null,
    val titulo: String? = null,
    val codigoOA: String? = null,
    val objetivo: String? = null,
    val explicacion: String? = null,
    val ejemplo: String? = null,
    val instrucciones: String? = null,
    val general: String? = null
) {
    fun hayErrores() = listOf(
        asignatura, titulo, codigoOA, objetivo, explicacion, ejemplo, instrucciones, general
    ).any { it != null }
}

data class PreguntaForm(
    val texto: String = "",
    val a: String = "",
    val b: String = "",
    val c: String = "",
    val d: String = "",
    val correcta: Int = -1
)

data class PreguntaErrores(
    val texto: String? = null,
    val a: String? = null,
    val b: String? = null,
    val c: String? = null,
    val d: String? = null,
    val correcta: String? = null
) {
    fun hayErrores() = listOf(texto, a, b, c, d, correcta).any { it != null }
}

/** ViewModel del perfil UTP: unidades, preguntas y cambios de estado. */
class UtpViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = AppDatabase.getInstance(application).let {
        UnidadRepository(it.unidadDao(), it.preguntaDao())
    }

    val unidades: StateFlow<List<Unidad>> = repositorio.todas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun preguntas(unidadId: Int): Flow<List<Pregunta>> = repositorio.preguntasDe(unidadId)

    // ---------- Formulario de unidad ----------
    private val _formulario = MutableStateFlow(UnidadForm())
    val formulario: StateFlow<UnidadForm> = _formulario.asStateFlow()

    private val _errores = MutableStateFlow(UnidadErrores())
    val errores: StateFlow<UnidadErrores> = _errores.asStateFlow()

    private val _guardadoOk = MutableStateFlow(false)
    val guardadoOk: StateFlow<Boolean> = _guardadoOk.asStateFlow()

    private var editandoId = 0

    fun nuevoFormulario() {
        editandoId = 0
        _formulario.value = UnidadForm()
        _errores.value = UnidadErrores()
        _guardadoOk.value = false
    }

    fun cargarParaEditar(id: Int) {
        viewModelScope.launch {
            val u = repositorio.buscar(id) ?: return@launch
            editandoId = u.id
            _formulario.value = UnidadForm(
                asignatura = u.asignatura,
                titulo = u.titulo,
                codigoOA = u.codigoOA,
                objetivo = u.objetivo,
                explicacion = u.explicacion,
                ejemplo = u.ejemplo,
                instrucciones = u.instrucciones,
                imagenUri = u.imagenUri
            )
            _errores.value = UnidadErrores()
            _guardadoOk.value = false
        }
    }

    fun actualizar(cambio: (UnidadForm) -> UnidadForm) {
        _formulario.value = cambio(_formulario.value)
    }

    fun elegirImagen(uri: Uri) {
        viewModelScope.launch {
            val ruta = withContext(Dispatchers.IO) { Imagenes.copiarAInterno(getApplication(), uri) }
            if (ruta != null) {
                actualizar { it.copy(imagenUri = ruta) }
            } else {
                _errores.value = _errores.value.copy(general = "No se pudo cargar la imagen")
            }
        }
    }

    fun quitarImagen() {
        actualizar { it.copy(imagenUri = null) }
    }

    fun guardar(creadorId: Int) {
        val f = _formulario.value
        val errores = UnidadErrores(
            asignatura = Validadores.asignatura(f.asignatura),
            titulo = Validadores.tituloUnidad(f.titulo),
            codigoOA = Validadores.codigoOA(f.codigoOA),
            objetivo = Validadores.objetivo(f.objetivo),
            explicacion = Validadores.explicacion(f.explicacion),
            ejemplo = Validadores.ejemplo(f.ejemplo),
            instrucciones = Validadores.instrucciones(f.instrucciones)
        )
        _errores.value = errores
        if (errores.hayErrores()) return

        viewModelScope.launch {
            try {
                val error = repositorio.guardar(
                    Unidad(
                        id = editandoId,
                        asignatura = f.asignatura,
                        titulo = f.titulo.trim(),
                        codigoOA = Validadores.normalizarOA(f.codigoOA),
                        objetivo = f.objetivo.trim(),
                        explicacion = f.explicacion.trim(),
                        ejemplo = f.ejemplo.trim(),
                        instrucciones = f.instrucciones.trim(),
                        imagenUri = f.imagenUri,
                        creadorId = creadorId
                    )
                )
                if (error == null) _guardadoOk.value = true
                else _errores.value = errores.copy(general = error)
            } catch (e: Exception) {
                _errores.value = errores.copy(general = "No se pudo guardar la unidad")
            }
        }
    }

    fun consumirGuardado() {
        _guardadoOk.value = false
    }

    // ---------- Estado y preguntas (pantalla de detalle) ----------
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun cambiarEstado(unidadId: Int, nuevo: EstadoUnidad) {
        viewModelScope.launch {
            _mensaje.value = try {
                repositorio.cambiarEstado(unidadId, nuevo)
            } catch (e: Exception) {
                "No se pudo cambiar el estado"
            }
        }
    }

    fun validarPregunta(f: PreguntaForm) = PreguntaErrores(
        texto = Validadores.textoPregunta(f.texto),
        a = Validadores.alternativaCorta(f.a),
        b = Validadores.alternativaCorta(f.b),
        c = Validadores.alternativaCorta(f.c),
        d = Validadores.alternativaCorta(f.d),
        correcta = Validadores.correcta(f.correcta)
    )

    fun agregarPregunta(unidadId: Int, f: PreguntaForm, esPruebaFinal: Boolean) {
        viewModelScope.launch {
            _mensaje.value = try {
                repositorio.agregarPregunta(
                    Pregunta(
                        unidadId = unidadId,
                        texto = f.texto.trim(),
                        alternativaA = f.a.trim(),
                        alternativaB = f.b.trim(),
                        alternativaC = f.c.trim(),
                        alternativaD = f.d.trim(),
                        correcta = f.correcta,
                        esPruebaFinal = esPruebaFinal
                    )
                )
            } catch (e: Exception) {
                "No se pudo guardar la pregunta"
            }
        }
    }

    fun eliminarPregunta(preguntaId: Int) {
        viewModelScope.launch {
            _mensaje.value = try {
                repositorio.eliminarPregunta(preguntaId)
            } catch (e: Exception) {
                "No se pudo eliminar la pregunta"
            }
        }
    }
}