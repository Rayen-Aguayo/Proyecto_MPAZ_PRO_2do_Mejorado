package com.example.escuela_escritora_marcela_paz.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.util.LectorVoz

/** Crea un lector de voz que se libera solo al salir de la pantalla. */
@Composable
fun rememberLector(): LectorVoz {
    val context = LocalContext.current
    val lector = remember { LectorVoz(context) }
    DisposableEffect(Unit) { onDispose { lector.liberar() } }
    return lector
}

/** Botón "Escuchar": lee el texto en voz alta (TextToSpeech). */
@Composable
fun BotonAudio(
    texto: String,
    lector: LectorVoz,
    etiqueta: String = "Escuchar",
    modifier: Modifier = Modifier
) {
    OutlinedButton(onClick = { lector.hablar(texto) }, modifier = modifier) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(etiqueta)
    }
}