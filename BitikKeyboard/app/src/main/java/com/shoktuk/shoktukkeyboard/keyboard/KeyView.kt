package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
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
    private val handler = Handler(Looper.getMainLooper())
    private val overlayHideDelay = 100L // ms
    private var style = KeyboardTheme.getLetterButtonStyle(context)

    init {
        setupView()
        applyStyle()
        setupContent()
        setupTouchHandling()
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
            if (isCaps && key.upperCaseColor != null && key.upperCaseColor >= 0) {
                fillColor = KeyboardTheme.colorIndexes[key.upperCaseColor]
            } else if (!isCaps && key.lowerCaseColor != null && key.lowerCaseColor >= 0) {
                fillColor = KeyboardTheme.colorIndexes[key.lowerCaseColor]
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

        subText.text = if (isCaps) key.upperCaseHint ?: "" else key.lowerCaseHint ?: ""
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

                else -> false
            }
        }

        setOnClickListener {
            val letter = (if (isCaps) key.uppercase else key.lowercase)?.replace("\uD803\\", "") ?: ""
            onKeyClick(letter)
        }

        if (key.lowercaseHold != null || key.uppercaseHold != null) {
            setOnLongClickListener {
                val letter = if (isCaps) key.uppercaseHold else key.lowercaseHold
                onLongPress(letter)
                true
            }
        }
    }

    private fun showOverlay() {
        removeOverlay()

        overlayView = LayoutInflater.from(context).inflate(R.layout.keyboard_key, null, false).apply {
            val overlayStyle = style.copy(
                textSizeSp = style.textSizeSp * 1.5f, fillColor = style.fillColor
            )

            findViewById<TextView>(R.id.mainText).apply {
                text = if (isCaps) key.uppercase else key.lowercase
                setTextColor(overlayStyle.textColor.toColorInt())
                textSize = overlayStyle.textSizeSp
            }

            findViewById<TextView>(R.id.subText).visibility = View.GONE

            val location = IntArray(2)
            getLocationInWindow(location)

            val overlayWidth = (width * 1.5f).toInt()
            val overlayHeight = (height * 1.5f).toInt()

            (context as? InputMethodService)?.window?.window?.decorView?.let { decorView ->
                (decorView as ViewGroup).addView(
                    this, FrameLayout.LayoutParams(
                        overlayWidth, overlayHeight
                    ).apply {
                        leftMargin = location[0] - (overlayWidth - width) / 2
                        topMargin = location[1] - overlayHeight - 20
                    })
            }
        }
    }

    private fun removeOverlay() {
        overlayView?.let { overlay ->
            handler.removeCallbacksAndMessages(null)
            (overlay.parent as? ViewGroup)?.removeView(overlay)
            overlayView = null
        }
    }

    private fun isWithinBounds(event: MotionEvent): Boolean {
        return event.x >= 0 && event.x < width && event.y >= 0 && event.y < height
    }

    fun cleanup() {
        removeOverlay()
        handler.removeCallbacksAndMessages(null)
    }
}