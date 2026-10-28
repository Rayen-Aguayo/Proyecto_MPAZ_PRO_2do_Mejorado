package com.example.escuela_escritora_marcela_paz.util

/** Flujo de una unidad didáctica: UTP la crea, la envía a revisión, la autoriza y puede deshabilitarla. */
enum class EstadoUnidad(val texto: String) {
    BORRADOR("Borrador"),
    EN_REVISION("En revisión"),
    AUTORIZADA("Autorizada"),
    DESHABILITADA("Deshabilitada");

    /** A qué estados se puede pasar desde este (nadie se salta pasos). */
    fun siguientesPermitidos(): List<EstadoUnidad> = when (this) {
        BORRADOR -> listOf(EN_REVISION)
        EN_REVISION -> listOf(AUTORIZADA, BORRADOR)
        AUTORIZADA -> listOf(DESHABILITADA)
        DESHABILITADA -> listOf(AUTORIZADA)
    }

    /** Una unidad autorizada no se edita: primero hay que deshabilitarla. */
    fun sePuedeEditar(): Boolean = this != AUTORIZADA

    companion object {
        fun desde(nombre: String): EstadoUnidad =
            entries.firstOrNull { it.name == nombre } ?: BORRADOR
    }
}