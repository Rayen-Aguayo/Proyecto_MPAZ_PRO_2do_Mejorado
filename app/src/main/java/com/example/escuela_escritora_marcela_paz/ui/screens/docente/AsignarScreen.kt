package com.example.escuela_escritora_marcela_paz.ui.screens.docente

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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.ui.components.BotonAnimado
import com.example.escuela_escritora_marcela_paz.ui.components.BotonFecha
import com.example.escuela_escritora_marcela_paz.ui.components.Cabecera
import com.example.escuela_escritora_marcela_paz.ui.components.CampoTexto
import com.example.escuela_escritora_marcela_paz.ui.components.MensajeError
import com.example.escuela_escritora_marcela_paz.ui.components.OpcionesRadio
import com.example.escuela_escritora_marcela_paz.util.Cursos
import com.example.escuela_escritora_marcela_paz.util.Notificaciones
import com.example.escuela_escritora_marcela_paz.viewmodel.DocenteViewModel

/** Asignar una unidad autorizada a un curso: fechas, intentos y opciones de la prueba final. */
@Composable
fun AsignarScreen(docVm: DocenteViewModel, unidadId: Int, onVolver: () -> Unit) {
    val context = LocalContext.current
    val autorizadas by docVm.autorizadas.collectAsState()
    val errores by docVm.errores.collectAsState()
    val asignadoOk by docVm.asignadoOk.collectAsState()
    val unidad = autorizadas.firstOrNull { it.id == unidadId }

    var cursoIndice by rememberSaveable { mutableStateOf(-1) }
    var inicio by rememberSaveable { mutableStateOf<Long?>(null) }
    var fin by rememberSaveable { mutableStateOf<Long?>(null) }
    var intentos by rememberSaveable { mutableStateOf("3") }
    var notificar by rememberSaveable { mutableStateOf(true) }
    var permitirVer by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) { docVm.nuevaAsignacion() }

    LaunchedEffect(asignadoOk) {
        if (asignadoOk) {
            if (notificar) {
                Notificaciones.mostrar(
                    context,
                    "Unidad asignada",
                    "Se avisó al curso ${Cursos.todos.getOrNull(cursoIndice) ?: ""}: " +
                            "${unidad?.titulo ?: "nueva unidad"} ya está disponible."
                )
            }
            docVm.consumirAsignado()
            onVolver()
        }
    }

    Scaffold(topBar = { Cabecera("Asignar unidad", onVolver = onVolver) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (unidad == null) {
                Text("Esta unidad ya no está autorizada.")
                return@Column
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        unidad.titulo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("${unidad.asignatura} · ${unidad.codigoOA}")
                    Text(unidad.objetivo, style = MaterialTheme.typography.bodyMedium)
                }
            }

            OpcionesRadio(
                titulo = "Curso",
                opciones = Cursos.todos,
                seleccion = cursoIndice,
                onSeleccion = { cursoIndice = it },
                error = errores.curso
            )

            Text("Rango de disponibilidad", style = MaterialTheme.typography.titleMedium)
            BotonFecha("Desde", inicio, errores.fechaInicio) { inicio = it }
            BotonFecha("Hasta", fin, errores.fechaFin) { fin = it }

            CampoTexto(
                intentos, { v -> if (v.length <= 1 && v.all { it.isDigit() }) intentos = v },
                "Intentos máximos de la prueba final (1 a 3)", errores.intentos,
                teclado = KeyboardType.Number
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Avisar al curso (notificación)")
                Switch(checked = notificar, onCheckedChange = { notificar = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("El estudiante puede revisar sus respuestas")
                Switch(checked = permitirVer, onCheckedChange = { permitirVer = it })
            }

            MensajeError(errores.general)
            Spacer(Modifier.height(4.dp))

            BotonAnimado(
                texto = "Asignar unidad",
                onClick = {
                    docVm.asignar(
                        unidadId = unidad.id,
                        curso = Cursos.todos.getOrNull(cursoIndice) ?: "",
                        inicio = inicio,
                        fin = fin,
                        intentos = intentos,
                        notificar = notificar,
                        permitirVerRespuestas = permitirVer
                    )
                }
            )
        }
    }
}
