package com.example.escuela_escritora_marcela_paz.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.escuela_escritora_marcela_paz.ui.screens.auth.LoginScreen
import com.example.escuela_escritora_marcela_paz.ui.screens.auth.RegistroScreen
import com.example.escuela_escritora_marcela_paz.ui.screens.docente.AsignacionDetalleScreen
import com.example.escuela_escritora_marcela_paz.ui.screens.docente.AsignarScreen
import com.example.escuela_escritora_marcela_paz.ui.screens.docente.DocenteHomeScreen
import com.example.escuela_escritora_marcela_paz.ui.screens.docente.EstudianteInformeScreen
import com.example.escuela_escritora_marcela_paz.ui.screens.estudiante.EstudianteHomeScreen
import com.example.escuela_escritora_marcela_paz.ui.screens.estudiante.PruebaFinalScreen
import com.example.escuela_escritora_marcela_paz.ui.screens.estudiante.UnidadEstudianteScreen
import com.example.escuela_escritora_marcela_paz.ui.screens.utp.UnidadDetalleScreen
import com.example.escuela_escritora_marcela_paz.ui.screens.utp.UnidadFormScreen
import com.example.escuela_escritora_marcela_paz.ui.screens.utp.UtpHomeScreen
import com.example.escuela_escritora_marcela_paz.viewmodel.AuthViewModel
import com.example.escuela_escritora_marcela_paz.viewmodel.DocenteViewModel
import com.example.escuela_escritora_marcela_paz.viewmodel.EstudianteViewModel
import com.example.escuela_escritora_marcela_paz.viewmodel.UtpViewModel

private fun enteroArg(nombre: String) = navArgument(nombre) { type = NavType.IntType }

@Composable
fun AppNavGraph(authVm: AuthViewModel = viewModel()) {
    val sesionLista by authVm.sesionLista.collectAsState()
    val usuario by authVm.usuario.collectAsState()

    // Mientras se revisa si había una sesión guardada, se muestra un indicador.
    if (!sesionLista) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val nav = rememberNavController()
    val utpVm: UtpViewModel = viewModel()
    val docVm: DocenteViewModel = viewModel()
    val estVm: EstudianteViewModel = viewModel()

    // La pantalla inicial se decide una sola vez: sesión guardada -> su home, si no -> login.
    val inicio = remember { usuario?.let { Rutas.home(it.rol) } ?: Rutas.LOGIN }

    NavHost(navController = nav, startDestination = inicio) {
        composable(Rutas.LOGIN) {
            LoginScreen(authVm, onIrRegistro = { nav.navigate(Rutas.REGISTRO) })
        }
        composable(Rutas.REGISTRO) {
            RegistroScreen(authVm, onVolver = { nav.popBackStack() })
        }

        // ---------------- UTP ----------------
        composable(Rutas.UTP_HOME) {
            UtpHomeScreen(
                authVm, utpVm,
                onNueva = { nav.navigate(Rutas.utpForm(0)) },
                onAbrir = { id -> nav.navigate(Rutas.utpDetalle(id)) }
            )
        }
        composable(Rutas.UTP_FORM, arguments = listOf(enteroArg("id"))) { e ->
            UnidadFormScreen(
                authVm, utpVm,
                unidadId = e.arguments?.getInt("id") ?: 0,
                onVolver = { nav.popBackStack() }
            )
        }
        composable(Rutas.UTP_DETALLE, arguments = listOf(enteroArg("id"))) { e ->
            UnidadDetalleScreen(
                utpVm,
                unidadId = e.arguments?.getInt("id") ?: 0,
                onEditar = { id -> nav.navigate(Rutas.utpForm(id)) },
                onVolver = { nav.popBackStack() }
            )
        }

        // ---------------- Docente ----------------
        composable(Rutas.DOCENTE_HOME) {
            DocenteHomeScreen(
                authVm, docVm,
                onAsignar = { id -> nav.navigate(Rutas.docenteAsignar(id)) },
                onAbrirAsignacion = { id -> nav.navigate(Rutas.docenteAsignacion(id)) }
            )
        }
        composable(Rutas.DOCENTE_ASIGNAR, arguments = listOf(enteroArg("unidadId"))) { e ->
            AsignarScreen(
                docVm,
                unidadId = e.arguments?.getInt("unidadId") ?: 0,
                onVolver = { nav.popBackStack() }
            )
        }
        composable(Rutas.DOCENTE_ASIGNACION, arguments = listOf(enteroArg("id"))) { e ->
            val asignacionId = e.arguments?.getInt("id") ?: 0
            AsignacionDetalleScreen(
                docVm,
                asignacionId = asignacionId,
                onAbrirEstudiante = { estId ->
                    nav.navigate(Rutas.docenteInforme(asignacionId, estId))
                },
                onVolver = { nav.popBackStack() }
            )
        }
        composable(
            Rutas.DOCENTE_INFORME,
            arguments = listOf(enteroArg("asignacionId"), enteroArg("estudianteId"))
        ) { e ->
            EstudianteInformeScreen(
                docVm,
                asignacionId = e.arguments?.getInt("asignacionId") ?: 0,
                estudianteId = e.arguments?.getInt("estudianteId") ?: 0,
                onVolver = { nav.popBackStack() }
            )
        }

        // ---------------- Estudiante ----------------
        composable(Rutas.ESTUDIANTE_HOME) {
            EstudianteHomeScreen(
                authVm, estVm,
                onAbrir = { id -> nav.navigate(Rutas.estudianteUnidad(id)) }
            )
        }
        composable(Rutas.ESTUDIANTE_UNIDAD, arguments = listOf(enteroArg("asignacionId"))) { e ->
            UnidadEstudianteScreen(
                estVm,
                asignacionId = e.arguments?.getInt("asignacionId") ?: 0,
                onPrueba = { id -> nav.navigate(Rutas.estudiantePrueba(id)) },
                onVolver = { nav.popBackStack() }
            )
        }
        composable(Rutas.ESTUDIANTE_PRUEBA, arguments = listOf(enteroArg("asignacionId"))) { e ->
            PruebaFinalScreen(
                estVm,
                asignacionId = e.arguments?.getInt("asignacionId") ?: 0,
                onVolver = { nav.popBackStack() }
            )
        }
    }

    // Reacciona a login / logout: cambia de pantalla según el rol.
    LaunchedEffect(usuario) {
        val actual = nav.currentDestination?.route
        val u = usuario
        if (u == null) {
            if (actual != Rutas.LOGIN && actual != Rutas.REGISTRO) {
                nav.navigate(Rutas.LOGIN) {
                    popUpTo(nav.graph.id) { inclusive = true }
                }
            }
        } else if (actual == Rutas.LOGIN || actual == Rutas.REGISTRO) {
            nav.navigate(Rutas.home(u.rol)) {
                popUpTo(nav.graph.id) { inclusive = true }
            }
        }
    }
}
