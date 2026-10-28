package com.example.escuela_escritora_marcela_paz.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.util.EstadoUnidad

/** Etiqueta de color con el estado de una unidad. */
@Composable
fun EtiquetaEstado(estado: EstadoUnidad) {
    val (fondo, texto) = when (estado) {
        EstadoUnidad.BORRADOR -> Color(0xFFE0E0E0) to Color(0xFF424242)
        EstadoUnidad.EN_REVISION -> Color(0xFFFFE9B3) to Color(0xFF6B4A00)
        EstadoUnidad.AUTORIZADA -> Color(0xFFCDEFD6) to Color(0xFF14532D)
        EstadoUnidad.DESHABILITADA -> Color(0xFFF8D0D0) to Color(0xFF7A1B1B)
    }
    Surface(color = fondo, shape = RoundedCornerShape(50)) {
        Text(
            estado.texto,
            color = texto,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}