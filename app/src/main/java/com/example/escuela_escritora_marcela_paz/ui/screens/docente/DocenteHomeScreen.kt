package com.example.escuela_escritora_marcela_paz.ui.screens.docente

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.entity.AsignacionConUnidad
import com.example.escuela_escritora_marcela_paz.entity.ResumenOA
import com.example.escuela_escritora_marcela_paz.entity.Unidad
import com.example.escuela_escritora_marcela_paz.ui.components.Cabecera
import com.example.escuela_escritora_marcela_paz.util.Calificacion
import com.example.escuela_escritora_marcela_paz.util.Cursos
import com.example.escuela_escritora_marcela_paz.util.Fechas
import com.example.escuela_escritora_marcela_paz.viewmodel.AuthViewModel
import com.example.escuela_escritora_marcela_paz.viewmodel.DocenteViewModel

/** Panel docente: unidades autorizadas, asignaciones con monitoreo y rendimiento por OA. */
@Composable
fun DocenteHomeScreen(
    authVm: AuthViewModel,
    docVm: DocenteViewModel,
    onAsignar: (Int) -> Unit,
    onAbrirAsignacion: (Int) -> Unit
) {
    val usuario by authVm.usuario.collectAsState()
    val autorizadas by docVm.autorizadas.collectAsState()
    val asignaciones by docVm.asignaciones.collectAsState()
    val resumen by docVm.resumenOA.collectAsState()
    val cursoFiltro by docVm.cursoFiltro.collectAsState()

    LaunchedEffect(usuario?.id) { usuario?.id?.let { docVm.iniciar(it) } }

    var pestana by rememberSaveable { mutableStateOf(0) }
    val titulos = listOf("Unidades", "Asignaciones", "Rendimiento")

    Scaffold(
        topBar = {
            Column {
                Cabecera("Panel docente", onSalir = { authVm.cerrarSesion() })
                TabRow(selectedTabIndex = pestana) {
                    titulos.forEachIndexed { i, titulo ->
                        Tab(
                            selected = pestana == i,
                            onClick = { pestana = i },
                            text = { Text(titulo) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (pestana) {
                0 -> TabUnidades(autorizadas, onAsignar)
                1 -> TabAsignaciones(asignaciones, onAbrirAsignacion)
                else -> TabRendimiento(resumen, cursoFiltro) { docVm.filtrarCurso(it) }
            }
        }
    }
}

@Composable
private fun TabUnidades(unidades: List<Unidad>, onAsignar: (Int) -> Unit) {
    Text(
        "Unidades autorizadas por UTP. Elige una para asignarla a un curso.",
        style = MaterialTheme.typography.bodyLarge
    )
    if (unidades.isEmpty()) {
        Text("Todavía no hay unidades autorizadas.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(unidades, key = { it.id }) { u ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        u.titulo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("${u.asignatura} · ${u.codigoOA}")
                    Text(u.objetivo, style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(
                        onClick = { onAsignar(u.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Asignar a un curso") }
                }
            }
        }
    }
}

@Composable
private fun TabAsignaciones(lista: List<AsignacionConUnidad>, onAbrir: (Int) -> Unit) {
    if (lista.isEmpty()) {
        Text("Aún no has asignado unidades. Hazlo desde la pestaña Unidades.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(lista, key = { it.asignacion.id }) { item ->
            val a = item.asignacion
            Card(Modifier.fillMaxWidth().clickable { onAbrir(a.id) }) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        item.unidad.titulo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("${a.curso} · ${item.unidad.codigoOA}")
                    Text(
                        "Disponible: ${Fechas.formatear(a.fechaInicio)} al ${Fechas.formatear(a.fechaFin)}"
                    )
                    Text("Intentos máximos de la prueba final: ${a.maxIntentos}")
                    Text(
                        "Toca para monitorear el avance",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun TabRendimiento(
    resumen: List<ResumenOA>,
    cursoFiltro: String?,
    onCurso: (String?) -> Unit
) {
    Text(
        "Rendimiento de la prueba final agrupado por objetivo de aprendizaje.",
        style = MaterialTheme.typography.bodyLarge
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = cursoFiltro == null,
            onClick = { onCurso(null) },
            label = { Text("Todos") }
        )
        Cursos.todos.forEach { curso ->
            FilterChip(
                selected = cursoFiltro == curso,
                onClick = { onCurso(curso) },
                label = { Text(curso) }
            )
        }
    }
    if (resumen.isEmpty()) {
        Text("Todavía no hay unidades asignadas.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(resumen, key = { it.codigoOA + it.asignatura }) { r ->
            val progreso = if (r.intentos == 0) 0f else ((r.promedio - 1.0) / 6.0).toFloat()
            val animado by animateFloatAsState(targetValue = progreso, label = "progresoOA")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "${r.codigoOA} · ${r.asignatura}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(r.objetivo, style = MaterialTheme.typography.bodyMedium)
                    Text("Estudiantes que rindieron: ${r.estudiantes} · Intentos: ${r.intentos}")
                    if (r.intentos == 0) {
                        Text("Sin intentos todavía.")
                    } else {
                        Text("Nota promedio: ${Calificacion.formatear(r.promedio)}")
                        LinearProgressIndicator(
                            progress = { animado },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
