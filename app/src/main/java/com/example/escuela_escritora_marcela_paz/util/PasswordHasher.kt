package com.example.escuela_escritora_marcela_paz.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    fun nuevaSal(): String {
        val sal = ByteArray(16)
        SecureRandom().nextBytes(sal)
        return Base64.encodeToString(sal, Base64.NO_WRAP)
    }

    fun hashear(clave: String, sal: String): String {
        val spec = PBEKeySpec(
            clave.toCharArray(),
            Base64.decode(sal, Base64.NO_WRAP),
            10_000,
            256
        )
        val hash = SecretKeyFactory
            .getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    fun verificar(clave: String, sal: String, hashGuardado: String): Boolean =
        hashear(clave, sal) == hashGuardado
}
