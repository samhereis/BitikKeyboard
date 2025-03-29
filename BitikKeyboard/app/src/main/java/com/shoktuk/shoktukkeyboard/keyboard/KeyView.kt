package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.R

class KeyView(
    context: Context,
    private val key: KeyEntry,
    private val isCaps: Boolean,
    private val buttonHeight: Int,
    private val margin: Int,
    private val onKeyClick: (String) -> Unit,
    private val onLongPress: (String?) -> Unit
) : FrameLayout(context) {

    private var overlayView: View? = null
    private var popupWindow: PopupWindow? = null
    private val handler = Handler(Looper.getMainLooper())
    private val overlayHideDelay = 100L // ms
    private var style = KeyboardTheme.getLetterButtonStyle(context)

    init {
        setupView()
        applyStyle()
        setupContent()
        setupTouchHandling()

        cleanup()
    }

    private fun setupView() {
        layoutParams = LinearLayout.LayoutParams(
            KeyboardTheme.getLetterButtonWidth(context), buttonHeight
        ).apply {
            marginStart = margin
            marginEnd = margin
        }
        setPadding(0, 0, 0, 0)
    }

    private fun applyStyle() {
        style = KeyboardTheme.getLetterButtonStyle(context).apply {
            if (isCaps && key.backgroundColorIndex_uppercase != null && key.backgroundColorIndex_uppercase >= 0) {
                fillColor = KeyboardTheme.colorIndexes[key.backgroundColorIndex_uppercase]
            } else if (!isCaps && key.backgroundColorIndex_lowercase != null && key.backgroundColorIndex_lowercase >= 0) {
                fillColor = KeyboardTheme.colorIndexes[key.backgroundColorIndex_lowercase]
            }
        }
        background = KeyboardTheme.createDrawableFromStyle(context, style)
    }

    private fun setupContent() {
        val view = LayoutInflater.from(context).inflate(R.layout.keyboard_key, this, true)
        val mainText = view.findViewById<TextView>(R.id.mainText)
        val subText = view.findViewById<TextView>(R.id.subText)

        mainText.text = if (isCaps) key.uppercase else key.lowercase
        mainText.setTextColor(style.textColor.toColorInt())
        mainText.textSize = style.textSizeSp

        subText.text = if (isCaps) key.upperCaseRomanization ?: "" else key.lowerCaseRomanization ?: ""
        subText.setTextColor(Color.White.toArgb())
        subText.textSize = KeyboardTheme.getHintButtonTextSize(context)
    }

    private fun setupTouchHandling() {
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    showOverlay()
                    true
                }

                MotionEvent.ACTION_UP -> {
                    handler.postDelayed(::removeOverlay, overlayHideDelay)
                    if (isWithinBounds(event)) {
                        performClick()
                    }
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    handler.postDelayed(::removeOverlay, overlayHideDelay)
                    true
                }

                else -> {
                    false
                }
            }
        }
        setOnClickListener {
            val letter = (if (isCaps) key.uppercase else key.lowercase)?.replace("\uD803\\", "") ?: ""
            onKeyClick(letter)
        }

        if (key.lowerCaseHold != null || key.upperCaseHold != null) {
            setOnLongClickListener {
                val letter = if (isCaps) key.upperCaseHold else key.lowerCaseHold
                onLongPress(letter)
                removeOverlay()
                true
            }
        }
    }

    private fun showOverlay() {
        removeOverlay()

        val overlayStyle = style.copy(
            textSizeSp = style.textSizeSp * 1.5f, fillColor = style.fillColor
        )
        val overlayView = LayoutInflater.from(context).inflate(R.layout.keyboard_key, null, false).apply {
            findViewById<TextView>(R.id.mainText).apply {
                text = if (isCaps) key.uppercase else key.lowercase
                setTextColor(overlayStyle.textColor.toColorInt())
                textSize = overlayStyle.textSizeSp
            }
            findViewById<TextView>(R.id.subText).visibility = View.GONE
            background = this@KeyView.background.constantState?.newDrawable()
        }
        val overlayWidth = (width * 1.5f).toInt()
        val overlayHeight = (height * 1.5f).toInt()

        popupWindow = PopupWindow(overlayView, overlayWidth, overlayHeight).apply {
            isOutsideTouchable = true
            isFocusable = false
        }

        val location = IntArray(2)
        getLocationInWindow(location)

        Log.d("KeyView", "Location for key: ${key.lowercase} x:${location[0]} y: ${location[1]}")
        val x = location[0] - (overlayWidth - width) / 2
        val y = location[1] - overlayHeight - 20

        (context as? InputMethodService)?.window?.window?.decorView?.post {
            Log.d("KeyView", "popupWindow?.showAtLocation called for key: ${key.lowercase} x: $x y: $y")
            popupWindow?.showAtLocation(
                (context as? InputMethodService)?.window?.window?.decorView,
                Gravity.NO_GRAVITY,
                x,
                y
            )
        }
    }

    private fun removeOverlay() {
        handler.removeCallbacksAndMessages(null)
        popupWindow?.dismiss()
        popupWindow = null
    }

    private fun isWithinBounds(event: MotionEvent): Boolean {
        return event.x >= 0 && event.x < width && event.y >= 0 && event.y < height
    }

    fun cleanup() {
        removeOverlay()
        handler.removeCallbacksAndMessages(null)
    }
}