package com.example.escuela_escritora_marcela_paz.ui.screens.utp

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.ui.components.Cabecera
import com.example.escuela_escritora_marcela_paz.ui.components.EtiquetaEstado
import com.example.escuela_escritora_marcela_paz.util.EstadoUnidad
import com.example.escuela_escritora_marcela_paz.viewmodel.AuthViewModel
import com.example.escuela_escritora_marcela_paz.viewmodel.UtpViewModel

@Composable
fun UtpHomeScreen(
    authVm: AuthViewModel,
    utpVm: UtpViewModel,
    onNueva: () -> Unit,
    onAbrir: (Int) -> Unit
) {
    val usuario by authVm.usuario.collectAsState()
    val unidades by utpVm.unidades.collectAsState()

    Scaffold(
        topBar = { Cabecera("Panel UTP", onSalir = { authVm.cerrarSesion() }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNueva,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva unidad") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Hola, ${usuario?.nombre ?: ""}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            val autorizadas = unidades.count { it.estadoUnidad() == EstadoUnidad.AUTORIZADA }
            val enRevision = unidades.count { it.estadoUnidad() == EstadoUnidad.EN_REVISION }
            Text("Unidades: ${unidades.size} · En revisión: $enRevision · Autorizadas: $autorizadas")

            if (unidades.isEmpty()) {
                Text("Aún no hay unidades. Crea la primera con el botón de abajo.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(unidades, key = { it.id }) { unidad ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .clickable { onAbrir(unidad.id) }
                        ) {
                            Column(
                                Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        unidad.titulo,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    EtiquetaEstado(unidad.estadoUnidad())
                                }
                                Text(
                                    "${unidad.asignatura} · ${unidad.codigoOA}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    unidad.objetivo,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
