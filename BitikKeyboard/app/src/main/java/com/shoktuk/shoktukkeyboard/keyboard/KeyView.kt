package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.R
import com.shoktuk.shoktukkeyboard.keyboard.KeyboardTheme.dpToPx

class KeyView(
    context: Context,
    private val key: KeyEntry,
    private var isCaps: Boolean,
    private val buttonHeight: Int,
    private val margin: Int,
    private val onKeyClick: (String) -> Unit,
    private val onLongPress: (String?) -> Unit
) : FrameLayout(context) {

    private val handler = Handler(Looper.getMainLooper())
    private val longPressDelay = 500L
    private val overlayHideDelay = 100L

    private var isTouchInBounds = false
    private var isLongPressed = false
    private var currentBackgroundColorIndex = 0
    private var popupWindow: PopupWindow? = null

    // State properties
    private lateinit var mainText: TextView
    private lateinit var subText: TextView
    private var style = KeyboardTheme.getLetterButtonStyle(context)

    init {
        setupView()
        applyStyle()
        setupContent()
        setupTouchHandling()
        setupClickListeners()
    }

    private fun setupView() {
        layoutParams = LinearLayout.LayoutParams(
            KeyboardTheme.getLetterButtonWidth(context), buttonHeight
        ).apply {
            marginStart = margin
            marginEnd = margin
        }
        foregroundGravity = Gravity.CENTER
        setPadding(0, 0, 0, 0)
    }

    private fun applyStyle() {
        currentBackgroundColorIndex = when {
            isCaps -> key.backgroundColorIndex_uppercase ?: 0
            else -> key.backgroundColorIndex_lowercase ?: 0
        }

        style = KeyboardTheme.getLetterButtonStyle(context).copy(
            fillColor = KeyboardTheme.colorIndexes[currentBackgroundColorIndex]
        )
        background = KeyboardTheme.createDrawableFromStyle(context, style)
    }

    private fun setupContent() {
        removeAllViews()
        val view = LayoutInflater.from(context).inflate(R.layout.keyboard_key, this, true)
        mainText = view.findViewById(R.id.mainText)
        subText = view.findViewById(R.id.subText)
        updateContent()
        addHoldIndicatorIfNeeded(view)
    }

    private fun updateContent() {
        mainText.text = getCurrentMainText()
        mainText.setTextColor(style.textColor.toColorInt())
        mainText.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.textSizeSp)

        subText.text = getCurrentSubText()
        subText.setTextColor(Color.WHITE)
        subText.setTextSize(TypedValue.COMPLEX_UNIT_SP, KeyboardTheme.getHintButtonTextSize(context))
        subText.visibility = if (subText.text.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    private fun setupClickListeners() {
        setOnClickListener {
            var text = getCurrentMainText()
            if (isLongPressed) {
                text = getCurrentMainText_Hold()
            }

            text = text?.replace("\uD803\\", "")

            onKeyClick(text!!)
        }
    }

    private fun setupTouchHandling() {
        val longPressRunnable = Runnable {
            var hasHoldValue: Boolean = false;

            if (isCaps && key.upperCaseHold != null) {
                hasHoldValue = true
            }
            if (!isCaps && key.lowerCaseHold != null) {
                hasHoldValue = true
            }

            if (isTouchInBounds && hasHoldValue) {
                isLongPressed = true
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                onLongPress(getCurrentMainText_Hold())
                applyStyle()
                updateContent()
                updateOverlay()
            }
        }

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isTouchInBounds = true
                    handler.postDelayed(longPressRunnable, longPressDelay)
                    showOverlay()
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val inBounds = event.x in 0f..width.toFloat() && event.y in 0f..height.toFloat()
                    if (isTouchInBounds != inBounds) {
                        isTouchInBounds = inBounds
                        if (!isTouchInBounds) {
                            handler.removeCallbacks(longPressRunnable)
                            dismissOverlay()
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacks(longPressRunnable)
                    dismissOverlay()
                    if (isTouchInBounds && !isLongPressed) {
                        performClick()
                    }
                    resetState()
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(longPressRunnable)
                    dismissOverlay()
                    resetState()
                    true
                }

                else -> false
            }
        }
    }

    private fun showOverlay() {
        dismissOverlay()

        if (isCaps) {
            if (key.upperCaseHold == null) {
                return; }
        } else {
            if (key.lowerCaseHold == null) {
                return; }
        }

        val overlayView = createOverlayView().apply {
            layoutParams = LayoutParams(
                width,  // Match key width
                height  // Match key height
            )
        }

        // Create popup with same dimensions as key
        popupWindow = PopupWindow(
            overlayView, (width * 1.25f).toInt(),  // Scale both dimensions equally
            (height * 1.25f).toInt()  // Maintain aspect ratio
        ).apply {
            isOutsideTouchable = true
            isFocusable = false
            elevation = 24f
        }

        val location = IntArray(2)
        getLocationInWindow(location)

        popupWindow?.showAtLocation(
            (context as? InputMethodService)?.window?.window?.decorView, Gravity.START or Gravity.TOP, location[0] - (popupWindow!!.width - width) / 2,  // Center horizontally
            location[1] - popupWindow!!.height - dpToPx(context, 16)  // Position above
        )
    }

    private fun createOverlayView(): View {
        return LayoutInflater.from(context).inflate(R.layout.keyboard_key, null).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
            )

            findViewById<TextView>(R.id.mainText).apply {
                text = getCurrentMainText()

                setTextColor(style.textColor.toColorInt())
                setTextSize(TypedValue.COMPLEX_UNIT_PX, style.textSizeSp * resources.displayMetrics.density * 1.5f)
            }

            findViewById<TextView>(R.id.subText).apply {
                text = getCurrentSubText()
                setTextColor(style.textColor.toColorInt())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, KeyboardTheme.getHintButtonTextSize(context))
            }
            background = KeyboardTheme.createDrawableFromStyle(context, style)
        }
    }

    private fun updateOverlay() {
        popupWindow?.contentView?.findViewById<TextView>(R.id.mainText)?.text = " " + getCurrentMainText_Hold() + " "
        popupWindow?.contentView?.findViewById<TextView>(R.id.subText)?.text = " " + getCurrentSubText_Hold() + " "

        var colorIndex: Int = 0;
        var colorToSet: Int = KeyboardTheme.colorIndexes[0].toColorInt()

        if (isCaps) {
            if (key.backgroundColorIndex_uppercase_Hold != null) {
                colorIndex = key.backgroundColorIndex_uppercase_Hold
            }

            if (key.backgroundColorIndex_uppercase_Hold != null) {
                colorToSet = KeyboardTheme.colorIndexes[key.backgroundColorIndex_uppercase_Hold].toColorInt()
            } else if (key.backgroundColorIndex_lowercase != null) {
                colorToSet = KeyboardTheme.colorIndexes[key.backgroundColorIndex_lowercase].toColorInt()
            }
        } else {
            if (key.backgroundColorIndex_lowercase_Hold != null) {
                colorIndex = key.backgroundColorIndex_lowercase_Hold
            }

            if (key.backgroundColorIndex_lowercase_Hold != null) {
                colorToSet = KeyboardTheme.colorIndexes[key.backgroundColorIndex_lowercase_Hold].toColorInt()
            } else if (key.backgroundColorIndex_lowercase != null) {
                colorToSet = KeyboardTheme.colorIndexes[key.backgroundColorIndex_lowercase].toColorInt()
            }
        }
        popupWindow?.contentView?.setBackgroundColor(colorToSet)
    }

    private fun addHoldIndicatorIfNeeded(root: View) {
        // Check if this key has any hold variants
        val hasHoldVariant = if (isCaps) {
            key.upperCaseHold != null
        } else {
            key.lowerCaseHold != null
        }

        if (hasHoldVariant) {
            val indicatorSize = dpToPx(context, 4) // 4dp dot
            val margin = dpToPx(context, 2) // 2dp offset from corner

            val indicator = View(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    indicatorSize, indicatorSize
                ).apply {
                    gravity = Gravity.TOP or Gravity.END
                    topMargin = margin
                    marginEnd = margin
                }

                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.WHITE)
                    setSize(indicatorSize, indicatorSize)
                }
            }

            // Add to the root view (which is the keyboard_key.xml FrameLayout)
            (root as ViewGroup).addView(indicator)
        }
    }

    private fun getCurrentMainText(): String? {
        if (isCaps) {
            return key.uppercase
        } else {
            return key.lowercase
        }
    }

    private fun getCurrentSubText(): CharSequence? {
        if (isCaps) {
            return key.upperCaseRomanization
        } else {
            return key.lowerCaseRomanization
        }
    }

    private fun getCurrentMainText_Hold(): String? {
        if (isCaps) {
            return key.upperCaseHold
        } else {
            return key.lowerCaseHold
        }
    }

    private fun getCurrentSubText_Hold(): CharSequence? {
        if (isCaps) {
            return key.upperCaseHoldHint ?: ""
        } else {
            return key.lowerCaseHoldHint ?: ""
        }
    }

    private fun dismissOverlay() {
        popupWindow?.dismiss()
        popupWindow = null
    }

    private fun resetState() {
        handler.postDelayed({
            isLongPressed = false
            applyStyle()
            updateContent()
        }, overlayHideDelay)
    }

    fun cleanup() {
        dismissOverlay()
        handler.removeCallbacksAndMessages(null)
    }
}