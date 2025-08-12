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
    private val isCLassic: Boolean,
    private val isTamga: Boolean,
    private val showTranscription: Boolean,
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

    private lateinit var mainText: TextView
    private lateinit var subText_bottom: TextView
    private lateinit var subText_top: TextView
    private var style = KeyboardTheme.getLetterButtonStyle_Normal(context, showTranscription)

    init {
        if (isTamga && isCaps && MyKeyboardService.currentAlphabet == "bitik") {
            style = KeyboardTheme.getLetterButtonStyle_UpperCase(context, showTranscription)
        }

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

        if (isTamga && isCaps) {
            style = KeyboardTheme.getLetterButtonStyle_UpperCase(context, showTranscription)
        }

        style = style.copy(fillColor = KeyboardTheme.colorIndexes[currentBackgroundColorIndex])
        background = KeyboardTheme.createDrawableFromStyle(context, style)
    }

    private fun setupContent() {
        removeAllViews()
        val view = LayoutInflater.from(context).inflate(R.layout.keyboard_key, this, true)
        mainText = view.findViewById(R.id.mainText)
        subText_bottom = view.findViewById(R.id.bottomSubText)
        subText_top = view.findViewById(R.id.topSubText)
        updateContent()
        addHoldIndicatorIfNeeded(view)
    }

    private fun updateContent() {
        mainText.text = getCurrentMainText()
        mainText.setTextColor(style.textColor.toColorInt())
        mainText.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.textSizeSp.value)
        mainText.setPadding(0, 0, 0, 0)

        var firstText = getCurrentSubText()?.toString().orEmpty()
        var secontText = getCurrentSubText_Alt()?.toString().orEmpty()

        if (isCLassic == false && MyKeyboardService.currentAlphabet == "bitik" && MyKeyboardService.currentMode == "letters") {
            secontText = ""
        }

        var lines = 1;
        if (isCLassic) {
            lines = 2
        }

        subText_bottom.apply {
            text = firstText
            maxLines = lines
            setLineSpacing(0.75f, 0.75f)
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, KeyboardTheme.getHintButtonTextSize(context).value)
            setPadding(0, -5, 0, KeyboardTheme.getHintButtonTextSize(context).value.toInt())
            visibility = if (firstText.isBlank()) View.GONE else View.VISIBLE
        }

        subText_top.apply {
            text = secontText
            maxLines = lines
            setLineSpacing(0.75f, 0.75f)
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, KeyboardTheme.getHintButtonTextSize(context).value)
            setPadding(0, KeyboardTheme.getHintButtonTextSize(context).value.toInt(), 0, -5)
            visibility = if (firstText.isBlank()) View.GONE else View.VISIBLE
        }

        if ( MyKeyboardService.currentAlphabet == "bitik" && MyKeyboardService.currentMode == "letters" && showTranscription == false) {
            subText_top.text = ""
            subText_bottom.text = ""
            mainText.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.textSizeSp.value)
        }
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

            TopRowBuilder_Old.onTypedListener?.invoke()
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

        val overlayView = createOverlayView().apply {
            layoutParams = LayoutParams(width, height)
        }

        popupWindow = PopupWindow(
            overlayView, (width * 1.25f).toInt(), (height * 1.25f).toInt()
        ).apply {
            isOutsideTouchable = true
            isFocusable = false
            elevation = 24f
        }

        val loc = IntArray(2)
        getLocationInWindow(loc)
        popupWindow!!.showAtLocation(
            (context as? InputMethodService)?.window?.window?.decorView, Gravity.START or Gravity.TOP, loc[0] - (popupWindow!!.width - width) / 2, loc[1] - popupWindow!!.height - dpToPx(context, 16)
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
                setTextSize(TypedValue.COMPLEX_UNIT_PX, style.textSizeSp.value * resources.displayMetrics.density * 1.5f)
            }

            findViewById<TextView>(R.id.bottomSubText).apply {
                var subText = if (isLongPressed) {
                    " " + getCurrentSubText_Hold() + " "
                } else {
                    " " + getCurrentSubText() + " "
                }

                text = subText
                maxLines = 2
                setTextColor(style.textColor.toColorInt())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, KeyboardTheme.getHintButtonTextSize(context).value)
                setPadding(0, 0, 0, KeyboardTheme.getHintButtonTextSize(context).value.toInt())
            }
            background = KeyboardTheme.createDrawableFromStyle(context, style)
        }
    }

    private fun updateOverlay() {
        var mainText = if (isLongPressed) {
            " " + getCurrentMainText_Hold() + " "
        } else {
            " " + getCurrentMainText() + " "
        }
        var subText = if (isLongPressed) {
            " " + getCurrentSubText_Hold() + " "
        } else {
            " " + getCurrentSubText() + " "
        }

        popupWindow?.contentView?.findViewById<TextView>(R.id.mainText)?.text = mainText
        popupWindow?.contentView?.findViewById<TextView>(R.id.bottomSubText)?.text = subText
        popupWindow?.contentView?.findViewById<TextView>(R.id.topSubText)?.text = ""

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
            return key.upperCaseRomanization ?: ""
        } else {
            return key.lowerCaseRomanization ?: ""
        }
    }

    private fun getCurrentSubText_Alt(): CharSequence? {
        if (isCaps) {
            return key.upperCaseRomanization_Alt ?: ""
        } else {
            return key.lowerCaseRomanization_Alt ?: ""
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
}