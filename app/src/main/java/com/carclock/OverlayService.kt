package com.carclock

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var audioManager: AudioManager
    private val handler = Handler(Looper.getMainLooper())

    private val clockRunnable = object : Runnable {
        override fun run() {
            updateClock()
            updateVolume()
            handler.postDelayed(this, 5000)
        }
    }

    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateWifi()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_bar, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            dpToPx(48),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START

        windowManager.addView(overlayView, params)

        setupButtons()

        val filter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(WifiManager.RSSI_CHANGED_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        }
        registerReceiver(wifiReceiver, filter)

        handler.post(clockRunnable)
    }

    private fun setupButtons() {
        overlayView.findViewById<View>(R.id.btnBack).setOnClickListener {
            CarAccessibilityService.pressBack()
        }
        overlayView.findViewById<View>(R.id.btnHome).setOnClickListener {
            CarAccessibilityService.pressHome()
        }
        overlayView.findViewById<View>(R.id.btnRecents).setOnClickListener {
            CarAccessibilityService.pressRecents()
        }
    }

    private fun updateClock() {
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEE dd/MM", Locale.getDefault())
        val now = Date()
        overlayView.findViewById<TextView>(R.id.tvTime).text = timeFmt.format(now)
        overlayView.findViewById<TextView>(R.id.tvDate).text = dateFmt.format(now)
    }

    private fun updateWifi() {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val icon = when {
            !wifiManager.isWifiEnabled -> "⊘"
            else -> {
                val rssi = wifiManager.connectionInfo.rssi
                when {
                    rssi >= -50 -> "▲▲▲"
                    rssi >= -70 -> "▲▲"
                    rssi >= -80 -> "▲"
                    else -> "~"
                }
            }
        }
        overlayView.findViewById<TextView>(R.id.tvWifi).text = icon
    }

    private fun updateVolume() {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val pct = if (max > 0) (cur * 100 / max) else 0
        val icon = when {
            pct == 0 -> "🔇"
            pct < 40 -> "🔈"
            pct < 70 -> "🔉"
            else -> "🔊"
        }
        overlayView.findViewById<TextView>(R.id.tvVolume).text = "$icon $pct%"
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        handler.removeCallbacks(clockRunnable)
        unregisterReceiver(wifiReceiver)
        if (::overlayView.isInitialized) windowManager.removeView(overlayView)
        super.onDestroy()
    }
}
