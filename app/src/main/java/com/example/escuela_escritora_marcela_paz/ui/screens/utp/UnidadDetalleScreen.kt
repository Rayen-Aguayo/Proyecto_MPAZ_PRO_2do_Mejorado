package com.example.escuela_escritora_marcela_paz.ui.screens.utp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.entity.Pregunta
import com.example.escuela_escritora_marcela_paz.ui.components.BotonAnimado
import com.example.escuela_escritora_marcela_paz.ui.components.Cabecera
import com.example.escuela_escritora_marcela_paz.ui.components.CampoTexto
import com.example.escuela_escritora_marcela_paz.ui.components.EtiquetaEstado
import com.example.escuela_escritora_marcela_paz.ui.components.ImagenUnidad
import com.example.escuela_escritora_marcela_paz.ui.components.MensajeError
import com.example.escuela_escritora_marcela_paz.ui.components.OpcionesRadio
import com.example.escuela_escritora_marcela_paz.util.EstadoUnidad
import com.example.escuela_escritora_marcela_paz.viewmodel.PreguntaErrores
import com.example.escuela_escritora_marcela_paz.viewmodel.PreguntaForm
import com.example.escuela_escritora_marcela_paz.viewmodel.UtpViewModel

/** Detalle de una unidad: contenido, preguntas y los cambios de estado (aprobar, deshabilitar...). */
@Composable
fun UnidadDetalleScreen(
    utpVm: UtpViewModel,
    unidadId: Int,
    onEditar: (Int) -> Unit,
    onVolver: () -> Unit
) {
    val unidades by utpVm.unidades.collectAsState()
    val unidad = unidades.firstOrNull { it.id == unidadId }
    val preguntas by remember(unidadId) { utpVm.preguntas(unidadId) }
        .collectAsState(initial = emptyList())
    val mensaje by utpVm.mensaje.collectAsState()

    var dialogoDesafio by remember { mutableStateOf(false) }
    var dialogoFinal by remember { mutableStateOf(false) }

    LaunchedEffect(unidadId) { utpVm.limpiarMensaje() }

    Scaffold(topBar = { Cabecera(unidad?.titulo ?: "Unidad", onVolver = onVolver) }) { padding ->
        if (unidad == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val estado = unidad.estadoUnidad()
        val editable = estado.sePuedeEditar()
        val desafios = preguntas.filter { !it.esPruebaFinal }
        val prueba = preguntas.filter { it.esPruebaFinal }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${unidad.asignatura} · ${unidad.codigoOA}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaEstado(estado)
            }

            MensajeError(mensaje)

            // ---- Contenido ----
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Texto("Objetivo de aprendizaje", unidad.objetivo)
                    Texto("Explicación", unidad.explicacion)
                    Texto("Ejemplo guiado", unidad.ejemplo)
                    Texto("Instrucciones (audio)", unidad.instrucciones)
                    ImagenUnidad(unidad.imagenUri)
                }
            }

            if (editable) {
                OutlinedButton(onClick = { onEditar(unidad.id) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Editar unidad")
                }
            } else {
                Text("Unidad autorizada: para editarla, primero deshabilítala.")
            }

            // ---- Cambios de estado ----
            estado.siguientesPermitidos().forEach { destino ->
                BotonAnimado(
                    texto = textoAccion(estado, destino),
                    onClick = { utpVm.cambiarEstado(unidad.id, destino) }
                )
            }

            // ---- Desafíos ----
            SeccionPreguntas(
                titulo = "Desafíos de práctica (${desafios.size})",
                preguntas = desafios,
                editable = editable,
                textoAgregar = "Agregar desafío",
                onAgregar = { dialogoDesafio = true },
                onEliminar = { utpVm.eliminarPregunta(it) }
            )

            // ---- Prueba final ----
            SeccionPreguntas(
                titulo = "Prueba final (${prueba.size})",
                preguntas = prueba,
                editable = editable,
                textoAgregar = "Agregar pregunta de prueba final",
                onAgregar = { dialogoFinal = true },
                onEliminar = { utpVm.eliminarPregunta(it) }
            )
        }

        if (dialogoDesafio) {
            PreguntaDialog(
                titulo = "Nuevo desafío",
                utpVm = utpVm,
                onCancelar = { dialogoDesafio = false },
                onGuardar = { f ->
                    utpVm.agregarPregunta(unidad.id, f, esPruebaFinal = false)
                    dialogoDesafio = false
                }
            )
        }
        if (dialogoFinal) {
            PreguntaDialog(
                titulo = "Nueva pregunta de prueba final",
                utpVm = utpVm,
                onCancelar = { dialogoFinal = false },
                onGuardar = { f ->
                    utpVm.agregarPregunta(unidad.id, f, esPruebaFinal = true)
                    dialogoFinal = false
                }
            )
        }
    }
}

private fun textoAccion(desde: EstadoUnidad, hasta: EstadoUnidad): String = when (hasta) {
    EstadoUnidad.EN_REVISION -> "Enviar a revisión"
    EstadoUnidad.AUTORIZADA ->
        if (desde == EstadoUnidad.EN_REVISION) "Aprobar y autorizar" else "Volver a autorizar"
    EstadoUnidad.BORRADOR -> "Devolver a borrador"
    EstadoUnidad.DESHABILITADA -> "Deshabilitar"
}

@Composable
private fun Texto(titulo: String, contenido: String) {
    Column {
        Text(titulo, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(contenido, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SeccionPreguntas(
    titulo: String,
    preguntas: List<Pregunta>,
    editable: Boolean,
    textoAgregar: String,
    onAgregar: () -> Unit,
    onEliminar: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        if (preguntas.isEmpty()) Text("Todavía no hay preguntas.")
        preguntas.forEachIndexed { i, p ->
            Card(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("${i + 1}. ${p.texto}", fontWeight = FontWeight.SemiBold)
                        p.alternativas().forEachIndexed { k, alt ->
                            val marca = if (k == p.correcta) "  ✔ correcta" else ""
                            Text("${"ABCD"[k]}) $alt$marca")
                        }
                    }
                    if (editable) {
                        IconButton(onClick = { onEliminar(p.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar pregunta")
                        }
                    }
                }
            }
        }
        if (editable) {
            OutlinedButton(onClick = onAgregar, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(textoAgregar)
            }
        }
    }
}

@Composable
private fun PreguntaDialog(
    titulo: String,
    utpVm: UtpViewModel,
    onCancelar: () -> Unit,
    onGuardar: (PreguntaForm) -> Unit
) {
    var form by remember { mutableStateOf(PreguntaForm()) }
    var errores by remember { mutableStateOf(PreguntaErrores()) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoTexto(
                    form.texto, { form = form.copy(texto = it) },
                    "Pregunta (10 a 150)", errores.texto,
                    singleLine = false, minLines = 2
                )
                CampoTexto(form.a, { form = form.copy(a = it) }, "Alternativa A", errores.a)
                CampoTexto(form.b, { form = form.copy(b = it) }, "Alternativa B", errores.b)
                CampoTexto(form.c, { form = form.copy(c = it) }, "Alternativa C", errores.c)
                CampoTexto(form.d, { form = form.copy(d = it) }, "Alternativa D", errores.d)
                OpcionesRadio(
                    titulo = "Respuesta correcta",
                    opciones = listOf("A", "B", "C", "D"),
                    seleccion = form.correcta,
                    onSeleccion = { form = form.copy(correcta = it) },
                    error = errores.correcta
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val e = utpVm.validarPregunta(form)
                errores = e
                if (!e.hayErrores()) onGuardar(form)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
