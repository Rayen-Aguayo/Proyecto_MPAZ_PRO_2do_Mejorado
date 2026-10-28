package com.example.escuela_escritora_marcela_paz.ui.screens.docente

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.entity.IntentoPrueba
import com.example.escuela_escritora_marcela_paz.repository.ItemRevision
import com.example.escuela_escritora_marcela_paz.ui.components.BotonAnimado
import com.example.escuela_escritora_marcela_paz.ui.components.Cabecera
import com.example.escuela_escritora_marcela_paz.ui.components.CampoTexto
import com.example.escuela_escritora_marcela_paz.ui.components.RevisionRespuestas
import com.example.escuela_escritora_marcela_paz.util.Calificacion
import com.example.escuela_escritora_marcela_paz.util.Fechas
import com.example.escuela_escritora_marcela_paz.viewmodel.DocenteViewModel

/** Informe de un estudiante: intentos, respuestas correctas/incorrectas y observaciones pedagógicas. */
@Composable
fun EstudianteInformeScreen(
    docVm: DocenteViewModel,
    asignacionId: Int,
    estudianteId: Int,
    onVolver: () -> Unit
) {
    val asignaciones by docVm.asignaciones.collectAsState()
    val item = asignaciones.firstOrNull { it.asignacion.id == asignacionId }
    val nombre by docVm.nombreEstudiante.collectAsState()

    LaunchedEffect(estudianteId) { docVm.cargarEstudiante(estudianteId) }

    Scaffold(topBar = { Cabecera(if (nombre.isBlank()) "Informe" else nombre, onVolver = onVolver) }) { padding ->
        if (item == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val a = item.asignacion
        val unidad = item.unidad

        val intentos by remember(a.id, estudianteId) { docVm.intentos(a.id, estudianteId) }
            .collectAsState(initial = emptyList())
        val observaciones by remember(unidad.id, estudianteId) {
            docVm.observaciones(estudianteId, unidad.id)
        }.collectAsState(initial = emptyList())

        var texto by remember { mutableStateOf("") }
        var errorTexto by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "${unidad.titulo} · ${a.curso}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text("${unidad.codigoOA}: ${unidad.objetivo}", style = MaterialTheme.typography.bodyMedium)

            // ---- Intentos y respuestas ----
            Text("Prueba final", style = MaterialTheme.typography.titleMedium)
            if (intentos.isEmpty()) {
                Text("Este estudiante aún no rinde la prueba.")
            }
            intentos.forEachIndexed { indice, intento ->
                TarjetaIntento(
                    numero = intentos.size - indice,
                    intento = intento,
                    cargarRevision = { docVm.revision(unidad.id, intento.id) }
                )
            }

            // ---- Observaciones pedagógicas ----
            Text("Observaciones pedagógicas", style = MaterialTheme.typography.titleMedium)
            if (observaciones.isEmpty()) Text("Todavía no hay observaciones.")
            observaciones.forEach { o ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(o.texto)
                        Text(
                            Fechas.formatearFechaHora(o.fecha),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            CampoTexto(
                texto, { texto = it; errorTexto = null },
                "Nueva observación (5 a 300)", errorTexto,
                singleLine = false, minLines = 3
            )
            BotonAnimado(
                texto = "Guardar observación",
                onClick = {
                    docVm.agregarObservacion(estudianteId, unidad.id, texto) { error ->
                        if (error == null) texto = "" else errorTexto = error
                    }
                }
            )
        }
    }
}

@Composable
private fun TarjetaIntento(
    numero: Int,
    intento: IntentoPrueba,
    cargarRevision: suspend () -> List<ItemRevision>
) {
    var abierto by remember { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth().clickable { abierto = !abierto }) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Intento $numero · Nota ${Calificacion.formatear(intento.nota)}",
                fontWeight = FontWeight.SemiBold
            )
            Text("${intento.aciertos} de ${intento.total} correctas · ${Fechas.formatearFechaHora(intento.fecha)}")
            Text(
                if (abierto) "Toca para ocultar las respuestas" else "Toca para revisar las respuestas",
                style = MaterialTheme.typography.bodySmall
            )
            if (abierto) {
                val revision by produceState<List<ItemRevision>>(emptyList(), intento.id) {
                    value = cargarRevision()
                }
                RevisionRespuestas(revision)
            }
        }
    }
}