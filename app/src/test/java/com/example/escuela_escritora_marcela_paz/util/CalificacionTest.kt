package com.example.escuela_escritora_marcela_paz.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CalificacionTest {

    @Test
    fun sesentaPorCientoEsNotaCuatro() {
        assertEquals(4.0, Calificacion.nota(6, 10), 0.0)
    }

    @Test
    fun todoCorrectoEsNotaSiete() {
        assertEquals(7.0, Calificacion.nota(10, 10), 0.0)
    }

    @Test
    fun todoIncorrectoEsNotaUno() {
        assertEquals(1.0, Calificacion.nota(0, 10), 0.0)
    }

    @Test
    fun sinPreguntasDevuelveNotaMinima() {
        assertEquals(1.0, Calificacion.nota(0, 0), 0.0)
    }

    @Test
    fun ochentaPorCientoEsCincoCinco() {
        assertEquals(5.5, Calificacion.nota(8, 10), 0.0)
    }

    @Test
    fun formateaConComa() {
        assertEquals("5,5", Calificacion.formatear(5.5))
    }
}
