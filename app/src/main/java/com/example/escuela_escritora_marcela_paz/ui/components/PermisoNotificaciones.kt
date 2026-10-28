package com.example.escuela_escritora_marcela_paz.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** Pide el permiso de notificaciones (solo Android 13+) una vez al entrar a la pantalla. */
@Composable
fun SolicitarPermisoNotificaciones() {
    if (Build.VERSION.SDK_INT < 33) return

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* si lo rechaza, la app sigue funcionando sin notificaciones */ }

    LaunchedEffect(Unit) {
        val concedido = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!concedido) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
