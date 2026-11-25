package com.example.escuela_escritora_marcela_paz.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val username: String, val password: String)

data class RegistroRequest(
    val nombre: String,
    val username: String,
    val password: String,
    val role: String,
    val curso: String? = null
)

/** Respuesta de POST /auth/login */
data class LoginResponse(
    val token: String,
    val id: Long,
    val nombre: String,
    val username: String,
    val role: String,
    val curso: String? = null
)

/** Respuesta de POST /auth/register */
data class UsuarioResponse(
    val id: Long,
    val nombre: String,
    val username: String,
    val role: String,
    val curso: String? = null
)

/** Endpoints del auth-service, siempre a través del api-gateway (puerto 9090). */
interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun registrar(@Body request: RegistroRequest): UsuarioResponse
}
