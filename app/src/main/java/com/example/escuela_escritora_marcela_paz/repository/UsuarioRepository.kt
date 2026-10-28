package com.example.escuela_escritora_marcela_paz.repository

import com.example.escuela_escritora_marcela_paz.data.local.dao.UsuarioDao
import com.example.escuela_escritora_marcela_paz.entity.Usuario
import com.example.escuela_escritora_marcela_paz.util.Cursos
import com.example.escuela_escritora_marcela_paz.util.PasswordHasher
import com.example.escuela_escritora_marcela_paz.util.Roles

class UsuarioRepository(private val dao: UsuarioDao) {

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
        return if (PasswordHasher.verificar(clave, usuario.sal, usuario.claveHash)) usuario else null
    }

    suspend fun buscarPorId(id: Int): Usuario? = dao.buscarPorId(id)

    /**
     * Cuentas de prueba (datos ficticios) para poder entrar a cada perfil.
     * Solo se crean si todavía no hay ningún usuario.
     */
    suspend fun sembrarCuentasDemo() {
        if (dao.cantidad() > 0) return
        registrar("Jefatura UTP (prueba)", "utp", "utp123", Roles.UTP)
        registrar("Docente (prueba)", "docente", "docente123", Roles.DOCENTE)
        registrar("Estudiante (prueba)", "alumno", "alumno123", Roles.ESTUDIANTE, Cursos.todos.first())
    }
}
