package com.example.escuela_escritora_marcela_paz.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.ui.components.BotonAnimado
import com.example.escuela_escritora_marcela_paz.ui.components.CampoTexto
import com.example.escuela_escritora_marcela_paz.ui.components.MensajeError
import com.example.escuela_escritora_marcela_paz.ui.components.SolicitarPermisoNotificaciones
import com.example.escuela_escritora_marcela_paz.viewmodel.AuthViewModel

@Composable
fun LoginScreen(authVm: AuthViewModel, onIrRegistro: () -> Unit) {
    SolicitarPermisoNotificaciones()

    val errores by authVm.errores.collectAsState()
    val cargando by authVm.cargando.collectAsState()

    var username by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var mostrarClave by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { authVm.limpiarErrores() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "MPAZ PRO 2.º",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text("Escuela Escritora Marcela Paz", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        CampoTexto(
            valor = username,
            onCambio = { username = it },
            etiqueta = "Usuario",
            error = errores.username
        )
        CampoTexto(
            valor = clave,
            onCambio = { clave = it },
            etiqueta = "Clave",
            error = errores.clave,
            esClave = true,
            mostrarClave = mostrarClave
        )
        TextButton(onClick = { mostrarClave = !mostrarClave }) {
            Text(if (mostrarClave) "Ocultar clave" else "Mostrar clave")
        }

        MensajeError(errores.general)

        BotonAnimado(
            texto = "Ingresar",
            onClick = { authVm.login(username, clave) },
            cargando = cargando
        )
        TextButton(onClick = onIrRegistro) { Text("Crear una cuenta") }
    }
}
