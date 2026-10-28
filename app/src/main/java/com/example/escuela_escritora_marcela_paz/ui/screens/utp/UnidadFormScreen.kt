package com.example.escuela_escritora_marcela_paz.ui.screens.utp

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.ui.components.BotonAnimado
import com.example.escuela_escritora_marcela_paz.ui.components.Cabecera
import com.example.escuela_escritora_marcela_paz.ui.components.CampoTexto
import com.example.escuela_escritora_marcela_paz.ui.components.ImagenUnidad
import com.example.escuela_escritora_marcela_paz.ui.components.MensajeError
import com.example.escuela_escritora_marcela_paz.ui.components.OpcionesRadio
import com.example.escuela_escritora_marcela_paz.util.Asignaturas
import com.example.escuela_escritora_marcela_paz.util.Notificaciones
import com.example.escuela_escritora_marcela_paz.viewmodel.AuthViewModel
import com.example.escuela_escritora_marcela_paz.viewmodel.UtpViewModel

/** Crear (unidadId = 0) o editar una unidad: OA, explicación escrita, ejemplo, audio e imagen. */
@Composable
fun UnidadFormScreen(
    authVm: AuthViewModel,
    utpVm: UtpViewModel,
    unidadId: Int,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val usuario by authVm.usuario.collectAsState()
    val form by utpVm.formulario.collectAsState()
    val errores by utpVm.errores.collectAsState()
    val guardadoOk by utpVm.guardadoOk.collectAsState()

    LaunchedEffect(unidadId) {
        if (unidadId == 0) utpVm.nuevoFormulario() else utpVm.cargarParaEditar(unidadId)
    }

    LaunchedEffect(guardadoOk) {
        if (guardadoOk) {
            Notificaciones.mostrar(
                context,
                if (unidadId == 0) "Unidad creada" else "Unidad actualizada",
                "La unidad \"${form.titulo.trim()}\" se guardó como borrador."
            )
            utpVm.consumirGuardado()
            onVolver()
        }
    }

    // Selector de imagen de la galería (no necesita permisos).
    val elegirImagen = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) utpVm.elegirImagen(uri) }

    Scaffold(
        topBar = {
            Cabecera(if (unidadId == 0) "Nueva unidad" else "Editar unidad", onVolver = onVolver)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OpcionesRadio(
                titulo = "Asignatura",
                opciones = Asignaturas.todas,
                seleccion = Asignaturas.todas.indexOf(form.asignatura),
                onSeleccion = { i -> utpVm.actualizar { it.copy(asignatura = Asignaturas.todas[i]) } },
                error = errores.asignatura
            )
            CampoTexto(
                form.titulo, { v -> utpVm.actualizar { it.copy(titulo = v) } },
                "Título de la unidad (5 a 60)", errores.titulo
            )
            CampoTexto(
                form.codigoOA, { v -> utpVm.actualizar { it.copy(codigoOA = v) } },
                "Código del objetivo (ej.: OA 3)", errores.codigoOA
            )
            CampoTexto(
                form.objetivo, { v -> utpVm.actualizar { it.copy(objetivo = v) } },
                "Objetivo de aprendizaje (10 a 150)", errores.objetivo,
                singleLine = false, minLines = 2
            )
            CampoTexto(
                form.explicacion, { v -> utpVm.actualizar { it.copy(explicacion = v) } },
                "Explicación escrita (20 a 300)", errores.explicacion,
                singleLine = false, minLines = 3
            )
            CampoTexto(
                form.ejemplo, { v -> utpVm.actualizar { it.copy(ejemplo = v) } },
                "Ejemplo guiado (5 a 200)", errores.ejemplo,
                singleLine = false, minLines = 2
            )
            CampoTexto(
                form.instrucciones, { v -> utpVm.actualizar { it.copy(instrucciones = v) } },
                "Instrucciones que se escuchan en audio (10 a 200)", errores.instrucciones,
                singleLine = false, minLines = 2
            )

            // Imagen de apoyo
            Text("Imagen de apoyo (opcional)")
            ImagenUnidad(form.imagenUri)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { elegirImagen.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) { Text(if (form.imagenUri == null) "Elegir imagen" else "Cambiar imagen") }
                if (form.imagenUri != null) {
                    TextButton(onClick = { utpVm.quitarImagen() }) { Text("Quitar") }
                }
            }

            MensajeError(errores.general)
            Spacer(Modifier.height(4.dp))

            BotonAnimado(
                texto = "Guardar unidad",
                onClick = { utpVm.guardar(usuario?.id ?: 0) }
            )
            Text(
                "Se guarda como borrador. Luego agrega desafíos y la prueba final, y envíala a revisión.",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}