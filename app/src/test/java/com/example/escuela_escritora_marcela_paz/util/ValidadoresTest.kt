package com.example.escuela_escritora_marcela_paz.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadoresTest {

    // ---------- username ----------
    @Test
    fun usernameVacioEsInvalido() {
        assertNotNull(Validadores.username(""))
    }

    @Test
    fun usernameCortoEsInvalido() {
        assertEquals("Debe tener al menos 4 caracteres", Validadores.username("abc"))
    }

    @Test
    fun usernameConEspaciosEsInvalido() {
        assertEquals("No puede tener espacios", Validadores.username("ana lopez"))
    }

    @Test
    fun usernameValido() {
        assertNull(Validadores.username("utp1"))
    }

    // ---------- clave ----------
    @Test
    fun claveCortaEsInvalida() {
        assertEquals("Debe tener al menos 6 caracteres", Validadores.clave("ab1"))
    }

    @Test
    fun claveSinNumeroEsInvalida() {
        assertEquals("Debe incluir al menos un número", Validadores.clave("abcdefg"))
    }

    @Test
    fun claveValida() {
        assertNull(Validadores.clave("clave123"))
    }

    // ---------- confirmación ----------
    @Test
    fun clavesDistintasNoCoinciden() {
        assertEquals("Las claves no coinciden", Validadores.confirmacion("clave123", "clave124"))
    }

    @Test
    fun clavesIgualesCoinciden() {
        assertNull(Validadores.confirmacion("clave123", "clave123"))
    }

    // ---------- rol y curso ----------
    @Test
    fun rolDesconocidoEsInvalido() {
        assertNotNull(Validadores.rol("ADMIN"))
    }

    @Test
    fun rolesConocidosSonValidos() {
        Roles.todos.forEach { assertNull(Validadores.rol(it)) }
    }

    @Test
    fun cursoFueraDeLaListaEsInvalido() {
        assertNotNull(Validadores.curso("3° Básico A"))
    }

    @Test
    fun cursoDeLaListaEsValido() {
        assertNull(Validadores.curso("2° Básico A"))
    }
}
