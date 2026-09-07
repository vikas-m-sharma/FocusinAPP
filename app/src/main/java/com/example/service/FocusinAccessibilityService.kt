package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.MainActivity
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

class FocusinAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var blockedPackageKeywords = listOf("instagram", "facebook", "tiktok", "twitter", "snapchat", "netflix", "reddit", "youtube")

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("FocusinAccessibility", "Accessibility service connected")
        refreshBlockedApps()
    }

    private fun refreshBlockedApps() {
        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val settings = db.userSettingsDao().getUserSettingsSync()
                val jsonStr = settings?.blockedAppsJson
                if (!jsonStr.isNullOrEmpty()) {
                    val arr = JSONArray(jsonStr)
                    val list = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        list.add(arr.getString(i).lowercase().trim())
                    }
                    if (list.isNotEmpty()) {
                        blockedPackageKeywords = list
                    }
                }
            } catch (e: Exception) {
                Log.e("FocusinAccessibility", "Error loading blocked apps", e)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkgName = event.packageName?.toString() ?: return

            // Don't intercept own app or Android system UI
            if (pkgName == packageName || pkgName.startsWith("com.android.systemui") || pkgName == "android") {
                return
            }

            val sessionState = FocusSessionService.sessionState.value
            // Only block when a focus session is actively running and not paused or on break
            if (sessionState.isActive && !sessionState.isPaused && !sessionState.isBreak) {
                val isDistracting = blockedPackageKeywords.any { keyword ->
                    pkgName.lowercase().contains(keyword)
                }

                if (isDistracting) {
                    Log.d("FocusinAccessibility", "Distracting app detected: $pkgName")
                    FocusSessionService.recordDistractionGlobal()

                    // Redirect back to Focusin
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("EXTRA_DISTRACTION_WARNING", true)
                        putExtra("EXTRA_BLOCKED_PACKAGE", pkgName)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d("FocusinAccessibility", "Accessibility service interrupted")
    }
}
