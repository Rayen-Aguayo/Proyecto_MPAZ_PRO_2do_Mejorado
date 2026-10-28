package com.example.escuela_escritora_marcela_paz.ui.screens.estudiante

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.ui.components.Cabecera
import com.example.escuela_escritora_marcela_paz.ui.components.SolicitarPermisoNotificaciones
import com.example.escuela_escritora_marcela_paz.ui.theme.ColoresAsignatura
import com.example.escuela_escritora_marcela_paz.util.Fechas
import com.example.escuela_escritora_marcela_paz.viewmodel.AuthViewModel
import com.example.escuela_escritora_marcela_paz.viewmodel.EstudianteViewModel

/** Mis asignaturas: solo las unidades que el docente asignó al curso del estudiante. */
@Composable
fun EstudianteHomeScreen(
    authVm: AuthViewModel,
    estVm: EstudianteViewModel,
    onAbrir: (Int) -> Unit
) {
    SolicitarPermisoNotificaciones()

    val usuario by authVm.usuario.collectAsState()
    val asignaciones by estVm.asignaciones.collectAsState()
    val conteos by estVm.conteos.collectAsState()

    LaunchedEffect(usuario) { usuario?.let { estVm.iniciar(it) } }

    Scaffold(
        topBar = { Cabecera("Mis asignaturas", onSalir = { authVm.cerrarSesion() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "¡Hola, ${usuario?.nombre ?: ""}!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            usuario?.curso?.let { Text(it, style = MaterialTheme.typography.titleMedium) }

            when {
                usuario?.curso == null -> Text(
                    "Tu cuenta no tiene curso. Regístrate como estudiante eligiendo tu curso.",
                    style = MaterialTheme.typography.titleMedium
                )
                asignaciones.isEmpty() -> Text(
                    "Todavía no tienes unidades. Tu profesora o profesor las irá habilitando.",
                    style = MaterialTheme.typography.titleMedium
                )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val grupos = asignaciones.groupBy { it.unidad.asignatura }
                    grupos.forEach { (asignatura, lista) ->
                        item(key = "asignatura_$asignatura") {
                            Text(
                                asignatura,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ColoresAsignatura.principal(asignatura)
                            )
                        }
                        items(lista, key = { it.asignacion.id }) { item ->
                            val a = item.asignacion
                            val usados = conteos[a.id] ?: 0
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAbrir(a.id) },
                                colors = CardDefaults.cardColors(
                                    containerColor = ColoresAsignatura.fondo(asignatura)
                                )
                            ) {
                                Column(
                                    Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        item.unidad.titulo,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text("${item.unidad.codigoOA}: ${item.unidad.objetivo}")
                                    Text(
                                        "Disponible hasta ${Fechas.formatear(a.fechaFin)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "Prueba final: $usados de ${a.maxIntentos} intentos usados",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
