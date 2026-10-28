package com.example.escuela_escritora_marcela_paz.ui.screens.docente

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.ui.components.Cabecera
import com.example.escuela_escritora_marcela_paz.util.Calificacion
import com.example.escuela_escritora_marcela_paz.util.Fechas
import com.example.escuela_escritora_marcela_paz.viewmodel.DocenteViewModel

/** Monitoreo: cada estudiante del curso con sus intentos y mejor nota en la prueba final. */
@Composable
fun AsignacionDetalleScreen(
    docVm: DocenteViewModel,
    asignacionId: Int,
    onAbrirEstudiante: (Int) -> Unit,
    onVolver: () -> Unit
) {
    val asignaciones by docVm.asignaciones.collectAsState()
    val item = asignaciones.firstOrNull { it.asignacion.id == asignacionId }

    Scaffold(topBar = { Cabecera(item?.unidad?.titulo ?: "Asignación", onVolver = onVolver) }) { padding ->
        if (item == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val a = item.asignacion
        val avance by remember(a.id, a.curso) { docVm.avanceCurso(a.id, a.curso) }
            .collectAsState(initial = emptyList())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "${a.curso} · ${item.unidad.codigoOA}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text("Disponible: ${Fechas.formatear(a.fechaInicio)} al ${Fechas.formatear(a.fechaFin)}")
            Text("Intentos máximos: ${a.maxIntentos}")

            Text("Estudiantes del curso", style = MaterialTheme.typography.titleMedium)
            if (avance.isEmpty()) {
                Text("No hay estudiantes registrados en este curso.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(avance, key = { it.estudianteId }) { e ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onAbrirEstudiante(e.estudianteId) }
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(e.nombre, fontWeight = FontWeight.SemiBold)
                                Text("Intentos: ${e.intentos} de ${a.maxIntentos}")
                                Text(
                                    if (e.intentos == 0) "Aún no rinde la prueba"
                                    else "Mejor nota: ${Calificacion.formatear(e.mejorNota)}"
                                )
                                Text(
                                    "Toca para ver respuestas y escribir observaciones",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}