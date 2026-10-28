package com.example.escuela_escritora_marcela_paz.util

object Roles {
    const val UTP = "UTP"
    const val DOCENTE = "DOCENTE"
    const val ESTUDIANTE = "ESTUDIANTE"
    val todos = listOf(UTP, DOCENTE, ESTUDIANTE)

    fun etiqueta(rol: String): String = when (rol) {
        UTP -> "Jefatura UTP"
        DOCENTE -> "Docente"
        else -> "Estudiante"
    }
}
