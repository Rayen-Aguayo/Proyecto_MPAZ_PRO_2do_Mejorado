package com.example.escuela_escritora_marcela_paz.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.util.Imagenes

/** Muestra la imagen de una unidad (ruta interna). No dibuja nada si no hay imagen. */
@Composable
fun ImagenUnidad(ruta: String?, descripcion: String = "Imagen de la unidad") {
    val imagen = remember(ruta) { Imagenes.cargar(ruta) } ?: return
    Image(
        bitmap = imagen,
        contentDescription = descripcion,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
            .clip(RoundedCornerShape(12.dp))
    )
}