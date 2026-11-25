package com.example.escuela_escritora_marcela_paz.repository

import com.example.escuela_escritora_marcela_paz.data.local.dao.UsuarioDao
import com.example.escuela_escritora_marcela_paz.data.remote.ApiClient
import com.example.escuela_escritora_marcela_paz.data.remote.AuthApi
import com.example.escuela_escritora_marcela_paz.data.remote.LoginRequest
import com.example.escuela_escritora_marcela_paz.data.remote.RegistroRequest
import com.example.escuela_escritora_marcela_paz.entity.Usuario
import com.example.escuela_escritora_marcela_paz.util.Cursos
import com.example.escuela_escritora_marcela_paz.util.PasswordHasher
import com.example.escuela_escritora_marcela_paz.util.Roles

/** Usuario ya autenticado en el servidor, junto con su token JWT. */
data class SesionRemota(val usuario: Usuario, val token: String)

class UsuarioRepository(
    private val dao: UsuarioDao,
    private val api: AuthApi = ApiClient.authApi
) {

    // ---------- Servidor (microservicios) ----------

    /**
     * Inicia sesión en el auth-service. Si responde bien, deja una copia del usuario
     * en Room (con el mismo id del servidor) para que el resto de la app siga funcionando.
     * Lanza HttpException (401, 400...) o IOException (sin conexión).
     */
    suspend fun loginRemoto(username: String, clave: String): SesionRemota {
        val r = api.login(LoginRequest(username.trim().lowercase(), clave))
        val usuario = Usuario(
            id = r.id.toInt(),
            nombre = r.nombre,
            username = r.username,
            sal = "",
            claveHash = "",
            rol = r.role,
            curso = r.curso
        )
        dao.guardar(usuario)
        return SesionRemota(usuario, r.token)
    }

    /** Registra el usuario en el auth-service. Lanza HttpException (409 si ya existe). */
    suspend fun registrarRemoto(
        nombre: String,
        username: String,
        clave: String,
        rol: String,
        curso: String? = null
    ) {
        api.registrar(
            RegistroRequest(
                nombre = nombre.trim(),
                username = username.trim().lowercase(),
                password = clave,
                role = rol,
                curso = if (rol == Roles.ESTUDIANTE) curso else null
            )
        )
    }

    // ---------- Local (Room): respaldo sin conexión ----------

    /** Devuelve false si el usuario ya existe. El curso solo aplica a estudiantes. */
    suspend fun registrar(
        nombre: String,
        username: String,
        clave: String,
        rol: String,
        curso: String? = null
    ): Boolean {
        val usernameLimpio = username.trim().lowercase()
        if (dao.buscarPorUsername(usernameLimpio) != null) return false

        val sal = PasswordHasher.nuevaSal()
        dao.insertar(
            Usuario(
                nombre = nombre.trim(),
                username = usernameLimpio,
                sal = sal,
                claveHash = PasswordHasher.hashear(clave, sal),
                rol = rol,
                curso = if (rol == Roles.ESTUDIANTE) curso else null
            )
        )
        return true
    }

    /** Devuelve el usuario si las credenciales son correctas, o null. */
    suspend fun login(username: String, clave: String): Usuario? {
        val usuario = dao.buscarPorUsername(username.trim().lowercase()) ?: return null
        if (usuario.sal.isEmpty()) return null // copia del servidor: no tiene clave local
        return if (PasswordHasher.verificar(clave, usuario.sal, usuario.claveHash)) usuario else null
    }

    suspend fun buscarPorId(id: Int): Usuario? = dao.buscarPorId(id)

    /**
     * Cuentas de prueba (datos ficticios) para poder entrar a cada perfil sin conexión.
     * Solo se crean si todavía no hay ningún usuario.
     */
    suspend fun sembrarCuentasDemo() {
        if (dao.cantidad() > 0) return
        registrar("Jefatura UTP (prueba)", "utp", "utp123", Roles.UTP)
        registrar("Docente (prueba)", "docente", "docente123", Roles.DOCENTE)
        registrar("Estudiante (prueba)", "alumno", "alumno123", Roles.ESTUDIANTE, Cursos.todos.first())
    }
}
