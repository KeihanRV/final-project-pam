package com.example.final_project_pam.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.final_project_pam.MainActivity

class AppMonitorService : AccessibilityService() {

    companion object {
        var instance: AppMonitorService? = null
        var allowedPackage: String? = null
        var isTimerRunning: Boolean = false
    }

    override fun onServiceConnected() {
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName || pkg == "com.android.systemui") return

        // Jika app ini ada di daftar monitored dan bukan yang diizinkan
        if (GatewayTimerService.monitoredPackages.contains(pkg)) {
            if (allowedPackage != pkg || !isTimerRunning) {
                forceOpenUnscroll()
            }
        }
    }

    fun forceOpenUnscroll() {
        allowedPackage = null
        isTimerRunning = false
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("from_monitor", true)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}