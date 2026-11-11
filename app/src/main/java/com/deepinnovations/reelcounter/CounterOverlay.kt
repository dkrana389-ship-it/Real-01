package com.deepinnovations.reelcounter

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

/** Instagram ke upar dikhne wala floating counter (drag karke kahin bhi rakh sakte ho). */
class CounterOverlay(private val service: AccessibilityService) {
    private val wm = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val density = service.resources.displayMetrics.density
    private fun dp(v: Int) = (v * density).toInt()

    private var attached = false
    private val countText = TextView(service)
    private val root = LinearLayout(service)

    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    )

    private var startX = 0
    private var startY = 0
    private var touchX = 0f
    private var touchY = 0f

    init {
        params.gravity = Gravity.TOP or Gravity.START
        params.x = Store.overlayX(service, dp(16))
        params.y = Store.overlayY(service, dp(70))

        root.orientation = LinearLayout.HORIZONTAL
        root.gravity = Gravity.CENTER_VERTICAL
        root.setPadding(dp(8), dp(6), dp(16), dp(6))
        root.alpha = 0.96f
        root.background = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(0xFF833AB4.toInt(), 0xFFFD1D1D.toInt(), 0xFFFCB045.toInt())
        ).apply {
            cornerRadius = dp(40).toFloat()
            setStroke(dp(1), 0x66FFFFFF)
        }

        val icon = TextView(service).apply {
            text = "▶"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(0xFFFD1D1D.toInt())
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.WHITE) }
            layoutParams = LinearLayout.LayoutParams(dp(28), dp(28))
        }
        countText.apply {
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            setPadding(dp(10), 0, dp(4), 0)
        }
        val label = TextView(service).apply {
            text = "reels"
            textSize = 12f
            setTextColor(0xCCFFFFFF.toInt())
        }
        root.addView(icon)
        root.addView(countText)
        root.addView(label)

        root.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x; startY = params.y
                    touchX = e.rawX; touchY = e.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX + (e.rawX - touchX).toInt()
                    params.y = startY + (e.rawY - touchY).toInt()
                    if (attached) wm.updateViewLayout(root, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    Store.setOverlayPos(service, params.x, params.y)
                    true
                }
                else -> false
            }
        }
    }

    fun show(count: Int) {
        countText.text = "$count"
        if (attached) return
        try {
            wm.addView(root, params)
            attached = true
        } catch (_: Exception) {
        }
    }

    fun hide() {
        if (!attached) return
        try {
            wm.removeView(root)
        } catch (_: Exception) {
        }
        attached = false
    }

    fun update(count: Int) {
        countText.text = "$count"
        root.animate().scaleX(1.2f).scaleY(1.2f).setDuration(110).withEndAction {
            root.animate().scaleX(1f).scaleY(1f).setDuration(140).start()
        }.start()
    }
}
