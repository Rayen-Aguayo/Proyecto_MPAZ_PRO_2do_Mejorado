package com.example.escuela_escritora_marcela_paz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.escuela_escritora_marcela_paz.repository.ItemRevision

private val LETRAS = listOf("A", "B", "C", "D")

/** Respuestas de un intento: correctas en verde, incorrectas en rojo con la respuesta buena. */
@Composable
fun RevisionRespuestas(items: List<ItemRevision>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEachIndexed { i, item ->
            val alternativas = item.pregunta.alternativas()
            val elegida = alternativas.getOrNull(item.elegida)?.let { "${LETRAS[item.elegida]}) $it" }
                ?: "Sin responder"
            val buena = "${LETRAS[item.pregunta.correcta]}) ${alternativas[item.pregunta.correcta]}"

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (item.correcta) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        if (item.correcta) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        contentDescription = if (item.correcta) "Correcta" else "Incorrecta"
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "${i + 1}. ${item.pregunta.texto}",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("Respondió: $elegida")
                        if (!item.correcta) Text("Respuesta correcta: $buena")
                    }
                }
            }
        }
    }
}