package com.example.escuela_escritora_marcela_paz.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.escuela_escritora_marcela_paz.data.local.dao.ActividadDao
import com.example.escuela_escritora_marcela_paz.data.local.dao.AsignacionDao
import com.example.escuela_escritora_marcela_paz.data.local.dao.IntentoDao
import com.example.escuela_escritora_marcela_paz.data.local.dao.IntentoPruebaDao
import com.example.escuela_escritora_marcela_paz.data.local.dao.ObservacionDao
import com.example.escuela_escritora_marcela_paz.data.local.dao.PreguntaDao
import com.example.escuela_escritora_marcela_paz.data.local.dao.UnidadDao
import com.example.escuela_escritora_marcela_paz.data.local.dao.UsuarioDao
import com.example.escuela_escritora_marcela_paz.entity.Actividad
import com.example.escuela_escritora_marcela_paz.entity.Asignacion
import com.example.escuela_escritora_marcela_paz.entity.AvanceDesafio
import com.example.escuela_escritora_marcela_paz.entity.Intento
import com.example.escuela_escritora_marcela_paz.entity.IntentoPrueba
import com.example.escuela_escritora_marcela_paz.entity.Observacion
import com.example.escuela_escritora_marcela_paz.entity.Pregunta
import com.example.escuela_escritora_marcela_paz.entity.RespuestaPrueba
import com.example.escuela_escritora_marcela_paz.entity.Unidad
import com.example.escuela_escritora_marcela_paz.entity.Usuario

@Database(
    entities = [
        Usuario::class, Actividad::class, Intento::class,
        Unidad::class, Pregunta::class, Asignacion::class, Observacion::class,
        IntentoPrueba::class, RespuestaPrueba::class, AvanceDesafio::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun actividadDao(): ActividadDao
    abstract fun intentoDao(): IntentoDao
    abstract fun unidadDao(): UnidadDao
    abstract fun preguntaDao(): PreguntaDao
    abstract fun asignacionDao(): AsignacionDao
    abstract fun observacionDao(): ObservacionDao
    abstract fun intentoPruebaDao(): IntentoPruebaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpaz_database"
                )
                    .fallbackToDestructiveMigration() // MVP: si cambias tablas, se recrea la BD
                    .build()
                    .also { INSTANCE = it }
            }
    }
}