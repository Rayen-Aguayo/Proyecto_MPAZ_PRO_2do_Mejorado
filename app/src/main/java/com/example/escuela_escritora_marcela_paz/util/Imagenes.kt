package com.example.escuela_escritora_marcela_paz.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

/** Guarda las imágenes de las unidades dentro de la app (sin librerías externas). */
object Imagenes {

    /** Copia la imagen elegida de la galería a la carpeta interna y devuelve su ruta. */
    fun copiarAInterno(context: Context, uri: Uri): String? = try {
        val carpeta = File(context.filesDir, "imagenes").apply { mkdirs() }
        val destino = File(carpeta, "unidad_${System.currentTimeMillis()}.img")
        val entrada = context.contentResolver.openInputStream(uri)
        if (entrada == null) {
            null
        } else {
            entrada.use { input -> destino.outputStream().use { input.copyTo(it) } }
            destino.absolutePath
        }
    } catch (e: Exception) {
        null
    }

    /** Carga la imagen reduciéndola para no gastar memoria de más. */
    fun cargar(ruta: String?, anchoMaximo: Int = 1024): ImageBitmap? {
        if (ruta.isNullOrBlank() || !File(ruta).exists()) return null
        return try {
            val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(ruta, medidas)
            var muestreo = 1
            while (medidas.outWidth / muestreo > anchoMaximo) muestreo *= 2
            val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
            BitmapFactory.decodeFile(ruta, opciones)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}