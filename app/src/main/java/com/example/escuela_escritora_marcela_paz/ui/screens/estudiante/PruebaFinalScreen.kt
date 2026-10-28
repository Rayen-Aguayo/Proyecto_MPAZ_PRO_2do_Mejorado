package com.example.escuela_escritora_marcela_paz.ui.screens.estudiante

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.repository.ItemRevision
import com.example.escuela_escritora_marcela_paz.repository.ResultadoPrueba
import com.example.escuela_escritora_marcela_paz.ui.components.BotonAnimado
import com.example.escuela_escritora_marcela_paz.ui.components.BotonAudio
import com.example.escuela_escritora_marcela_paz.ui.components.Cabecera
import com.example.escuela_escritora_marcela_paz.ui.components.RevisionRespuestas
import com.example.escuela_escritora_marcela_paz.ui.components.rememberLector
import com.example.escuela_escritora_marcela_paz.util.Calificacion
import com.example.escuela_escritora_marcela_paz.viewmodel.EstudianteViewModel

/** Prueba final: intentos limitados, nota 1,0 a 7,0 y revisión de respuestas correctas/incorrectas. */
@Composable
fun PruebaFinalScreen(
    estVm: EstudianteViewModel,
    asignacionId: Int,
    onVolver: () -> Unit
) {
    val asignaciones by estVm.asignaciones.collectAsState()
    val conteos by estVm.conteos.collectAsState()
    val resultado by estVm.resultado.collectAsState()
    val revision by estVm.revision.collectAsState()
    val ultimo by estVm.ultimoIntento.collectAsState()
    val item = asignaciones.firstOrNull { it.asignacion.id == asignacionId }

    val lector = rememberLector()
    val respuestas = remember { mutableStateMapOf<Int, Int>() }
    var confirmar by remember { mutableStateOf(false) }

    LaunchedEffect(asignacionId) { estVm.limpiarPrueba() }

    Scaffold(topBar = { Cabecera("Prueba final", onVolver = onVolver) }) { padding ->
        if (item == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val a = item.asignacion
        val u = item.unidad
        val preguntas by remember(u.id) { estVm.preguntas(u.id) }
            .collectAsState(initial = emptyList())
        val prueba = preguntas.filter { it.esPruebaFinal }
        val usados = conteos[a.id] ?: 0
        val restantes = a.maxIntentos - usados

        // Si ya había intentos, se carga el último resultado guardado.
        LaunchedEffect(a.id, usados) {
            if (usados > 0 && estVm.resultado.value == null) estVm.cargarUltimoIntento(a)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(u.titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            val r = resultado
            when {
                r is ResultadoPrueba.Terminada -> {
                    TarjetaNota(
                        nota = r.nota,
                        detalle = "${r.aciertos} de ${r.total} correctas",
                        extra = if (r.intentosRestantes > 0) "Te quedan ${r.intentosRestantes} intentos."
                        else "Ya usaste todos tus intentos."
                    )
                    RevisionSiCorresponde(a.permitirVerRespuestas, revision)
                    BotonAnimado(texto = "Volver a la unidad", onClick = onVolver)
                }
                r is ResultadoPrueba.SinIntentos ->
                    Text("Ya usaste todos tus intentos.", style = MaterialTheme.typography.titleMedium)
                r is ResultadoPrueba.NoDisponible ->
                    Text(
                        "Esta prueba no está disponible en este momento.",
                        style = MaterialTheme.typography.titleMedium
                    )
                prueba.isEmpty() ->
                    Text("Esta unidad todavía no tiene prueba final.")
                restantes <= 0 -> {
                    Text("Ya usaste todos tus intentos.", style = MaterialTheme.typography.titleMedium)
                    ultimo?.let {
                        TarjetaNota(
                            nota = it.nota,
                            detalle = "${it.aciertos} de ${it.total} correctas",
                            extra = "Resultado de tu último intento."
                        )
                    }
                    RevisionSiCorresponde(a.permitirVerRespuestas, revision)
                }
                else -> {
                    Text(
                        "Intentos disponibles: $restantes de ${a.maxIntentos}. " +
                                "Responde todas las preguntas y entrega.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    prueba.forEachIndexed { i, p ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(
                                Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "${i + 1}. ${p.texto}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                val lectura = buildString {
                                    append(p.texto)
                                    p.alternativas().forEachIndexed { k, alt ->
                                        append(". Opción ${"ABCD"[k]}: $alt")
                                    }
                                }
                                BotonAudio(texto = lectura, lector = lector, etiqueta = "Escuchar")
                                p.alternativas().forEachIndexed { k, alt ->
                                    val fondo by animateColorAsState(
                                        targetValue = if (respuestas[p.id] == k)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        label = "fondoAlternativa"
                                    )
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = fondo),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { respuestas[p.id] = k }
                                    ) {
                                        Text(
                                            "${"ABCD"[k]}) $alt",
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Text("Respondidas: ${respuestas.size} de ${prueba.size}")
                    BotonAnimado(
                        texto = "Entregar prueba",
                        onClick = { confirmar = true },
                        enabled = respuestas.size == prueba.size
                    )
                }
            }
        }

        if (confirmar) {
            AlertDialog(
                onDismissRequest = { confirmar = false },
                title = { Text("¿Entregar la prueba?") },
                text = { Text("Usarás 1 de tus intentos. Después no podrás cambiar tus respuestas.") },
                confirmButton = {
                    TextButton(onClick = {
                        confirmar = false
                        estVm.rendir(a, respuestas.toMap())
                        respuestas.clear()
                    }) { Text("Entregar") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmar = false }) { Text("Seguir revisando") }
                }
            )
        }
    }
}

@Composable
private fun RevisionSiCorresponde(permitido: Boolean, revision: List<ItemRevision>) {
    if (!permitido) {
        Text("Tu docente no habilitó la revisión de respuestas para esta unidad.")
        return
    }
    if (revision.isEmpty()) return
    Text(
        "Revisa tus respuestas",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    RevisionRespuestas(revision)
}

@Composable
private fun TarjetaNota(nota: Double, detalle: String, extra: String) {
    val aprobado = nota >= 4.0
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (aprobado) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                if (aprobado) "¡Muy bien!" else "Sigue practicando",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Nota ${Calificacion.formatear(nota)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(detalle)
            Text(extra)
        }
    }
}
