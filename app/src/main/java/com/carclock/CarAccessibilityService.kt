package com.carclock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class CarAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        instance = null
    }

    companion object {
        var instance: CarAccessibilityService? = null

        fun pressBack() = instance?.performGlobalAction(GLOBAL_ACTION_BACK) ?: false
        fun pressHome() = instance?.performGlobalAction(GLOBAL_ACTION_HOME) ?: false
        fun pressRecents() = instance?.performGlobalAction(GLOBAL_ACTION_RECENTS) ?: false

        fun isEnabled() = instance != null
    }
}
