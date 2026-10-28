package com.example.escuela_escritora_marcela_paz.ui.navigation

import com.example.escuela_escritora_marcela_paz.util.Roles

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"

    // UTP
    const val UTP_HOME = "utp/home"
    const val UTP_FORM = "utp/unidad-form/{id}"       // id = 0 -> unidad nueva
    const val UTP_DETALLE = "utp/unidad/{id}"

    // Docente
    const val DOCENTE_HOME = "docente/home"
    const val DOCENTE_ASIGNAR = "docente/asignar/{unidadId}"
    const val DOCENTE_ASIGNACION = "docente/asignacion/{id}"
    const val DOCENTE_INFORME = "docente/asignacion/{asignacionId}/estudiante/{estudianteId}"

    // Estudiante
    const val ESTUDIANTE_HOME = "estudiante/home"
    const val ESTUDIANTE_UNIDAD = "estudiante/unidad/{asignacionId}"
    const val ESTUDIANTE_PRUEBA = "estudiante/prueba/{asignacionId}"

    fun utpForm(id: Int) = "utp/unidad-form/$id"
    fun utpDetalle(id: Int) = "utp/unidad/$id"
    fun docenteAsignar(unidadId: Int) = "docente/asignar/$unidadId"
    fun docenteAsignacion(id: Int) = "docente/asignacion/$id"
    fun docenteInforme(asignacionId: Int, estudianteId: Int) =
        "docente/asignacion/$asignacionId/estudiante/$estudianteId"
    fun estudianteUnidad(asignacionId: Int) = "estudiante/unidad/$asignacionId"
    fun estudiantePrueba(asignacionId: Int) = "estudiante/prueba/$asignacionId"

    fun home(rol: String): String = when (rol) {
        Roles.UTP -> UTP_HOME
        Roles.DOCENTE -> DOCENTE_HOME
        else -> ESTUDIANTE_HOME
    }
}

