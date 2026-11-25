package com.example.escuela_escritora_marcela_paz.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sesionDataStore by preferencesDataStore(name = "sesion")

class SesionDataStore(private val context: Context) {

    companion object {
        private val USUARIO_ID = intPreferencesKey("usuario_id")
        private val ROL = stringPreferencesKey("rol")
        private val NOMBRE = stringPreferencesKey("nombre")
        private val TOKEN = stringPreferencesKey("token")
    }

    val usuarioId: Flow<Int?> = context.sesionDataStore.data.map { it[USUARIO_ID] }
    val rol: Flow<String?> = context.sesionDataStore.data.map { it[ROL] }
    val nombre: Flow<String?> = context.sesionDataStore.data.map { it[NOMBRE] }

    /** Token JWT entregado por el login; se envía como "Authorization: Bearer ..." */
    val token: Flow<String?> = context.sesionDataStore.data.map { it[TOKEN] }

    suspend fun guardarSesion(id: Int, rol: String, nombre: String, token: String? = null) {
        context.sesionDataStore.edit {
            it[USUARIO_ID] = id
            it[ROL] = rol
            it[NOMBRE] = nombre
            if (token != null) it[TOKEN] = token else it.remove(TOKEN)
        }
    }

    suspend fun cerrarSesion() {
        context.sesionDataStore.edit { it.clear() }
    }
}
