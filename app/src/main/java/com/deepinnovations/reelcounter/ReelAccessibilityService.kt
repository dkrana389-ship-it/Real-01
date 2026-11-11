package com.deepinnovations.reelcounter

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.ContextCompat

class ReelAccessibilityService : AccessibilityService() {

    private val igPackages = setOf("com.instagram.android", "com.instagram.lite")
    private val handler = Handler(Looper.getMainLooper())
    private var overlay: CounterOverlay? = null
    private var inInstagram = false
    private var lastTick = 0L
    private var lastCountAt = 0L

    private val screenOff = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = leaveInstagram()
    }

    // Instagram khula hone ke dauran har second time jodta hai
    private val ticker = object : Runnable {
        override fun run() {
            if (!inInstagram) return
            flushTime()
            handler.postDelayed(this, 1000)
        }
    }

    // Scroll ruk jaane ke baad ek reel count hoti hai
    private val settle = Runnable { onScrollSettled() }

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlay = CounterOverlay(this)
        ContextCompat.registerReceiver(
            this, screenOff, IntentFilter(Intent.ACTION_SCREEN_OFF), ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val e = event ?: return
        val pkg = e.packageName?.toString() ?: return
        when (e.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (pkg in igPackages) enterInstagram()
                else if (!isIgnored(pkg)) leaveInstagram()
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                if (pkg in igPackages) {
                    enterInstagram()
                    handler.removeCallbacks(settle)
                    handler.postDelayed(settle, 400)
                }
            }
        }
    }

    private fun isIgnored(pkg: String) =
        pkg == "com.android.systemui" || pkg == "android" ||
            pkg.contains("inputmethod") || pkg.contains("keyboard")

    private fun enterInstagram() {
        if (inInstagram) return
        inInstagram = true
        lastTick = SystemClock.elapsedRealtime()
        handler.post(ticker)
        overlay?.show(Store.reels(this))
    }

    private fun leaveInstagram() {
        if (!inInstagram) return
        flushTime()
        inInstagram = false
        handler.removeCallbacks(ticker)
        handler.removeCallbacks(settle)
        overlay?.hide()
    }

    private fun flushTime() {
        val now = SystemClock.elapsedRealtime()
        Store.addMillis(this, now - lastTick)
        lastTick = now
    }

    private fun onScrollSettled() {
        if (!inInstagram) return
        val now = SystemClock.elapsedRealtime()
        if (now - lastCountAt < 600) return
        if (Store.strict(this) && !inReels()) return
        lastCountAt = now
        overlay?.update(Store.addReel(this))
    }

    private fun inReels(): Boolean = hasReelsPager(rootInActiveWindow, 0)

    private fun hasReelsPager(node: AccessibilityNodeInfo?, depth: Int): Boolean {
        if (node == null || depth > 25) return false
        val id = node.viewIdResourceName
        if (id != null && (id.contains("clips_viewer") || id.contains("clips_swipe"))) return true
        for (i in 0 until node.childCount) {
            if (hasReelsPager(node.getChild(i), depth + 1)) return true
        }
        return false
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        leaveInstagram()
        try { unregisterReceiver(screenOff) } catch (_: Exception) {}
        super.onDestroy()
    }
}
