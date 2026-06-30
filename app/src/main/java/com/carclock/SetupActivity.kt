package com.carclock

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Permite 'Mostrar sobre otras apps' y reinicia el auto", Toast.LENGTH_LONG).show()
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
            finish()
            return
        }

        startForegroundService(Intent(this, OverlayService::class.java))

        Toast.makeText(this, "CarClock activo. Activa el servicio de accesibilidad en Ajustes.", Toast.LENGTH_LONG).show()

        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

        finish()
    }
}
