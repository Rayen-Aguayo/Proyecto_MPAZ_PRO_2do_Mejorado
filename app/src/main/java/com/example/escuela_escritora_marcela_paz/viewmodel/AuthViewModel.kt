package com.example.escuela_escritora_marcela_paz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.escuela_escritora_marcela_paz.data.local.AppDatabase
import com.example.escuela_escritora_marcela_paz.entity.Usuario
import com.example.escuela_escritora_marcela_paz.repository.UsuarioRepository
import com.example.escuela_escritora_marcela_paz.session.SesionDataStore
import com.example.escuela_escritora_marcela_paz.util.Roles
import com.example.escuela_escritora_marcela_paz.util.Validadores
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

data class AuthErrores(
    val nombre: String? = null,
    val username: String? = null,
    val clave: String? = null,
    val confirmacion: String? = null,
    val rol: String? = null,
    val curso: String? = null,
    val general: String? = null
) {
    fun hayErrores() = listOf(nombre, username, clave, confirmacion, rol, curso, general)
        .any { it != null }
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio =
        UsuarioRepository(AppDatabase.getInstance(application).usuarioDao())
    private val sesion = SesionDataStore(application)

    private val _errores = MutableStateFlow(AuthErrores())
    val errores: StateFlow<AuthErrores> = _errores.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso: StateFlow<Boolean> = _registroExitoso.asStateFlow()

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    /** true cuando ya se revisó si había una sesión guardada. */
    private val _sesionLista = MutableStateFlow(false)
    val sesionLista: StateFlow<Boolean> = _sesionLista.asStateFlow()

    init {
        restaurarSesion()
    }

    private fun restaurarSesion() {
        viewModelScope.launch {
            try {
                // Cuentas de prueba (utp / docente / alumno) la primera vez que se abre la app.
                withContext(Dispatchers.Default) { repositorio.sembrarCuentasDemo() }

                val id = sesion.usuarioId.first()
                if (id != null) _usuario.value = repositorio.buscarPorId(id)
            } catch (e: Exception) {
                _usuario.value = null
            } finally {
                _sesionLista.value = true
            }
        }
    }

    fun registrar(
        nombre: String,
        username: String,
        clave: String,
        confirmacion: String,
        rol: String,
        curso: String = ""
    ) {
        val errores = AuthErrores(
            nombre = Validadores.nombre(nombre),
            username = Validadores.username(username),
            clave = Validadores.clave(clave),
            confirmacion = Validadores.confirmacion(clave, confirmacion),
            rol = Validadores.rol(rol),
            curso = if (rol == Roles.ESTUDIANTE) Validadores.curso(curso) else null
        )
        _errores.value = errores
        if (errores.hayErrores()) return

        viewModelScope.launch {
            _cargando.value = true
            try {
                repositorio.registrarRemoto(nombre, username, clave, rol, curso)
                _registroExitoso.value = true
            } catch (e: HttpException) {
                _errores.value = when (e.code()) {
                    409 -> errores.copy(username = "Ese usuario ya existe")
                    400 -> errores.copy(general = "Los datos no son válidos para el servidor")
                    else -> errores.copy(general = "Error del servidor (${e.code()})")
                }
            } catch (e: IOException) {
                _errores.value = errores.copy(general = "No se pudo conectar con el servidor")
            } catch (e: Exception) {
                _errores.value = errores.copy(general = "No se pudo completar el registro")
            } finally {
                _cargando.value = false
            }
        }
    }

    fun login(username: String, clave: String) {
        val errores = AuthErrores(
            username = if (username.isBlank()) "El usuario es obligatorio" else null,
            clave = if (clave.isBlank()) "La clave es obligatoria" else null
        )
        _errores.value = errores
        if (errores.hayErrores()) return

        viewModelScope.launch {
            _cargando.value = true
            try {
                val remota = repositorio.loginRemoto(username, clave)
                sesion.guardarSesion(
                    remota.usuario.id, remota.usuario.rol, remota.usuario.nombre, remota.token
                )
                _usuario.value = remota.usuario
            } catch (e: HttpException) {
                _errores.value = if (e.code() == 401) {
                    AuthErrores(general = "Usuario o clave incorrectos")
                } else {
                    AuthErrores(general = "Error del servidor (${e.code()})")
                }
            } catch (e: IOException) {
                // Sin conexión con el servidor: se intenta con las cuentas locales de prueba.
                iniciarSesionLocal(username, clave)
            } catch (e: Exception) {
                _errores.value = AuthErrores(general = "No se pudo iniciar sesión")
            } finally {
                _cargando.value = false
            }
        }
    }

    private suspend fun iniciarSesionLocal(username: String, clave: String) {
        val local = repositorio.login(username, clave)
        if (local == null) {
            _errores.value = AuthErrores(general = "No se pudo conectar con el servidor")
        } else {
            sesion.guardarSesion(local.id, local.rol, local.nombre)
            _usuario.value = local
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            sesion.cerrarSesion()
            _usuario.value = null
        }
    }

    fun limpiarErrores() {
        _errores.value = AuthErrores()
    }

    fun consumirRegistroExitoso() {
        _registroExitoso.value = false
    }
}