package com.example.escuela_escritora_marcela_paz.ui.screens.estudiante

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.entity.Pregunta
import com.example.escuela_escritora_marcela_paz.ui.components.BotonAnimado
import com.example.escuela_escritora_marcela_paz.ui.components.BotonAudio
import com.example.escuela_escritora_marcela_paz.ui.components.Cabecera
import com.example.escuela_escritora_marcela_paz.ui.components.ImagenUnidad
import com.example.escuela_escritora_marcela_paz.ui.components.rememberLector
import com.example.escuela_escritora_marcela_paz.ui.theme.ColoresAsignatura
import com.example.escuela_escritora_marcela_paz.util.LectorVoz
import com.example.escuela_escritora_marcela_paz.viewmodel.EstudianteViewModel

/** Contenido de una unidad asignada: texto breve, imagen, audios, ejemplo, desafíos y acceso a la prueba. */
@Composable
fun UnidadEstudianteScreen(
    estVm: EstudianteViewModel,
    asignacionId: Int,
    onPrueba: (Int) -> Unit,
    onVolver: () -> Unit
) {
    val asignaciones by estVm.asignaciones.collectAsState()
    val conteos by estVm.conteos.collectAsState()
    val item = asignaciones.firstOrNull { it.asignacion.id == asignacionId }
    val lector = rememberLector()

    Scaffold(topBar = { Cabecera(item?.unidad?.titulo ?: "Unidad", onVolver = onVolver) }) { padding ->
        if (item == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val u = item.unidad
        val a = item.asignacion

        val preguntas by remember(u.id) { estVm.preguntas(u.id) }
            .collectAsState(initial = emptyList())
        val avance by remember(u.id) { estVm.avance(u.id) }
            .collectAsState(initial = emptyList())

        val desafios = preguntas.filter { !it.esPruebaFinal }
        val hechos = avance.map { it.preguntaId }.toSet()
        val completados = desafios.count { it.id in hechos }
        val totalFinal = preguntas.count { it.esPruebaFinal }
        val usados = conteos[a.id] ?: 0
        val progreso by animateFloatAsState(
            targetValue = if (desafios.isEmpty()) 0f else completados.toFloat() / desafios.size,
            label = "progresoDesafios"
        )
        var desafioAbierto by remember { mutableStateOf<Pregunta?>(null) }
        val color = ColoresAsignatura.principal(u.asignatura)
        val fondo = ColoresAsignatura.fondo(u.asignatura)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "${u.asignatura} · ${u.codigoOA}",
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(u.objetivo, style = MaterialTheme.typography.titleLarge)

            ImagenUnidad(u.imagenUri)

            TarjetaLectura("Lee y aprende", u.explicacion, lector, fondo)
            TarjetaLectura("Instrucciones", u.instrucciones, lector, fondo)
            TarjetaLectura("Mira un ejemplo", u.ejemplo, lector, fondo)

            // ---- Desafíos progresivos ----
            Text(
                "Desafíos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (desafios.isEmpty()) {
                Text("Esta unidad todavía no tiene desafíos.")
            } else {
                Text("Completados: $completados de ${desafios.size}")
                LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())
            }
            desafios.forEachIndexed { i, p ->
                val hecho = p.id in hechos
                val desbloqueado = i == 0 || desafios[i - 1].id in hechos
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = desbloqueado) { desafioAbierto = p },
                    colors = CardDefaults.cardColors(
                        containerColor = if (hecho) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            when {
                                hecho -> Icons.Filled.CheckCircle
                                desbloqueado -> Icons.Filled.PlayArrow
                                else -> Icons.Filled.Lock
                            },
                            contentDescription = null
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Desafío ${i + 1}", fontWeight = FontWeight.SemiBold)
                            Text(
                                when {
                                    hecho -> "¡Completado!"
                                    desbloqueado -> "Toca para jugar"
                                    else -> "Termina el desafío anterior"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // ---- Prueba final ----
            Text(
                "Prueba final",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (totalFinal == 0) "Esta unidad todavía no tiene prueba final."
                else "Intentos usados: $usados de ${a.maxIntentos}"
            )
            BotonAnimado(
                texto = if (usados == 0) "Rendir prueba final" else "Ver prueba final y resultados",
                onClick = { onPrueba(a.id) },
                enabled = totalFinal > 0
            )
        }

        desafioAbierto?.let { p ->
            DesafioDialog(
                pregunta = p,
                lector = lector,
                onCorrecta = { estVm.marcarDesafio(p) },
                onCerrar = { desafioAbierto = null }
            )
        }
    }
}

@Composable
private fun TarjetaLectura(titulo: String, texto: String, lector: LectorVoz, fondo: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = fondo)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(texto, style = MaterialTheme.typography.bodyLarge)
            BotonAudio(texto = "$titulo. $texto", lector = lector)
        }
    }
}

@Composable
private fun DesafioDialog(
    pregunta: Pregunta,
    lector: LectorVoz,
    onCorrecta: () -> Unit,
    onCerrar: () -> Unit
) {
    var elegida by remember { mutableStateOf(-1) }
    var resultado by remember { mutableStateOf<Boolean?>(null) }

    val lectura = buildString {
        append(pregunta.texto)
        pregunta.alternativas().forEachIndexed { i, alt -> append(". Opción ${"ABCD"[i]}: $alt") }
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(pregunta.texto) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotonAudio(texto = lectura, lector = lector, etiqueta = "Escuchar la pregunta")

                pregunta.alternativas().forEachIndexed { i, alt ->
                    val fondoAlt by animateColorAsState(
                        targetValue = if (elegida == i) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        label = "fondoDesafio"
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = fondoAlt),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = resultado != true) {
                                elegida = i
                                resultado = null
                            }
                    ) {
                        Text(
                            "${"ABCD"[i]}) $alt",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                // Confirmación de avance simple
                AnimatedVisibility(
                    visible = resultado != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val bien = resultado == true
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (bien) MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (bien) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                if (bien) "¡Muy bien! Desafío completado." else "Casi. Intenta otra vez.",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (resultado == true) {
                TextButton(onClick = onCerrar) { Text("Seguir") }
            } else {
                TextButton(
                    enabled = elegida >= 0,
                    onClick = {
                        val ok = elegida == pregunta.correcta
                        resultado = ok
                        if (ok) onCorrecta()
                    }
                ) { Text("Responder") }
            }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}
