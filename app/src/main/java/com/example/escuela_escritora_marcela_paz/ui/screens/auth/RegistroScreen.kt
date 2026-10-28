package com.example.escuela_escritora_marcela_paz.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.ui.components.BotonAnimado
import com.example.escuela_escritora_marcela_paz.ui.components.Cabecera
import com.example.escuela_escritora_marcela_paz.ui.components.CampoTexto
import com.example.escuela_escritora_marcela_paz.ui.components.MensajeError
import com.example.escuela_escritora_marcela_paz.ui.components.OpcionesRadio
import com.example.escuela_escritora_marcela_paz.util.Cursos
import com.example.escuela_escritora_marcela_paz.util.Notificaciones
import com.example.escuela_escritora_marcela_paz.util.Roles
import com.example.escuela_escritora_marcela_paz.viewmodel.AuthViewModel

@Composable
fun RegistroScreen(authVm: AuthViewModel, onVolver: () -> Unit) {
    val context = LocalContext.current
    val errores by authVm.errores.collectAsState()
    val cargando by authVm.cargando.collectAsState()
    val registroExitoso by authVm.registroExitoso.collectAsState()

    var nombre by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }
    var mostrarClave by rememberSaveable { mutableStateOf(false) }
    var rolIndice by rememberSaveable { mutableStateOf(-1) }
    var cursoIndice by rememberSaveable { mutableStateOf(-1) }

    val rolElegido = Roles.todos.getOrNull(rolIndice) ?: ""

    LaunchedEffect(Unit) { authVm.limpiarErrores() }

    // Registro correcto: notificación local y vuelta al login.
    LaunchedEffect(registroExitoso) {
        if (registroExitoso) {
            Notificaciones.mostrar(
                context,
                "Registro exitoso",
                "Tu cuenta fue creada. Ya puedes ingresar."
            )
            authVm.consumirRegistroExitoso()
            onVolver()
        }
    }

    Scaffold(topBar = { Cabecera("Crear cuenta", onVolver = onVolver) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CampoTexto(nombre, { nombre = it }, "Nombre (ficticio)", errores.nombre)
            CampoTexto(username, { username = it }, "Usuario", errores.username)
            CampoTexto(
                clave, { clave = it }, "Clave (mín. 6, con un número)", errores.clave,
                esClave = true, mostrarClave = mostrarClave
            )
            CampoTexto(
                confirmacion, { confirmacion = it }, "Repite la clave", errores.confirmacion,
                esClave = true, mostrarClave = mostrarClave
            )
            TextButton(onClick = { mostrarClave = !mostrarClave }) {
                Text(if (mostrarClave) "Ocultar claves" else "Mostrar claves")
            }

            OpcionesRadio(
                titulo = "Perfil",
                opciones = Roles.todos.map { Roles.etiqueta(it) },
                seleccion = rolIndice,
                onSeleccion = { rolIndice = it },
                error = errores.rol
            )

            // El curso solo se pide a los estudiantes: define qué unidades verá.
            if (rolElegido == Roles.ESTUDIANTE) {
                OpcionesRadio(
                    titulo = "Curso",
                    opciones = Cursos.todos,
                    seleccion = cursoIndice,
                    onSeleccion = { cursoIndice = it },
                    error = errores.curso
                )
            }

            MensajeError(errores.general)
            Spacer(Modifier.height(4.dp))

            BotonAnimado(
                texto = "Registrarme",
                onClick = {
                    authVm.registrar(
                        nombre, username, clave, confirmacion,
                        rolElegido,
                        Cursos.todos.getOrNull(cursoIndice) ?: ""
                    )
                },
                cargando = cargando
            )
        }
    }
}
