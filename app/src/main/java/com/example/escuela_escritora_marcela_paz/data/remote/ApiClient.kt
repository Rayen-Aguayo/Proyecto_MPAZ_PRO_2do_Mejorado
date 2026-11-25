package com.example.escuela_escritora_marcela_paz.data.remote

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    /**
     * Dirección del api-gateway.
     * - Emulador de Android Studio: 10.0.2.2 es el localhost del PC.
     * - Celular físico: cambia por la IP de tu PC en la red WiFi, por ejemplo "http://192.168.1.50:9090/".
     */
    const val BASE_URL = "http://10.0.2.2:9090/"
}

object ApiClient {

    private val http: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
}
