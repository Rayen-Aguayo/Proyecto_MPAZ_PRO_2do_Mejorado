package com.example.escuela_escritora_marcela_paz.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Recurso nativo 1: lectura en voz alta (TextToSpeech de Android).
 * Solo reproduce audio: no usa micrófono ni pide permisos.
 */
class LectorVoz(context: Context) {

    private var tts: TextToSpeech? = null
    private var listo = false

    init {
        tts = TextToSpeech(context.applicationContext) { estado ->
            if (estado == TextToSpeech.SUCCESS) {
                val resultado = tts?.setLanguage(Locale("es", "CL"))
                if (resultado == TextToSpeech.LANG_MISSING_DATA ||
                    resultado == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    tts?.setLanguage(Locale("es"))
                }
                tts?.setSpeechRate(0.85f) // un poco más lento, para lectores iniciales
                listo = true
            }
        }
    }

    fun hablar(texto: String) {
        if (listo) tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "mpaz_lectura")
    }

    fun detener() {
        tts?.stop()
    }

    fun liberar() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        listo = false
    }
}
