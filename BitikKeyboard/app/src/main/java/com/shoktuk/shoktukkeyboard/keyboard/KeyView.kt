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
import androidx.core.widget.TextViewCompat
import com.shoktuk.shoktukkeyboard.R
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme.dpToPx

class KeyView(
    context: Context, private val key: KeyEntry, private val buttonHeight: Int, private val onKeyClick: (String) -> Unit, private val onLongPress: (String?) -> Unit
) : FrameLayout(context) {

    private val handler = Handler(Looper.getMainLooper())
    private val longPressDelay = 250L

    private var isTouchInBounds = false
    private var isLongPressed = false
    private var currentBackgroundColorIndex = 0
    private var popupWindow: PopupWindow? = null

    private lateinit var mainText: TextView
    private lateinit var subText_bottom: TextView
    private lateinit var subText_top: TextView
    private var style = KeyboardTheme.getLetterButtonStyle_Normal(context, MyKeyboardService.showLetterTranscription)

    private lateinit var visualContainer: FrameLayout
    private val visualInsetPx = dpToPx(context, 4)

    init {
        if (MyKeyboardService.isTamga && MyKeyboardService.isCaps && MyKeyboardService.currentAlphabet == "bitik") {
            style = KeyboardTheme.getLetterButtonStyle_UpperCase(context, MyKeyboardService.showLetterTranscription)
        }
        setupView()
        setupContent()
        setupTouchHandling()
        setupClickListeners()
    }

    private fun setupView() {
        layoutParams = LinearLayout.LayoutParams(
            KeyboardTheme.getLetterButtonWidth(context), buttonHeight
        ).apply {
            marginStart = 0
            marginEnd = 0
        }
        foregroundGravity = Gravity.CENTER
        setPadding(0, 0, 0, 0)
        setBackgroundColor(Color.TRANSPARENT)
    }

    private fun applyStyle() {
        currentBackgroundColorIndex = if (MyKeyboardService.isCaps) {
            key.backgroundColorIndex_uppercase ?: 1
        } else {
            key.backgroundColorIndex_lowercase ?: 1
        }

        if (MyKeyboardService.isTamga && MyKeyboardService.isCaps) {
            style = KeyboardTheme.getLetterButtonStyle_UpperCase(context, MyKeyboardService.showLetterTranscription)
        }

        style = style.copy(fillColor = KeyboardTheme.getColor(currentBackgroundColorIndex))

        if (::visualContainer.isInitialized) {
            visualContainer.background = KeyboardTheme.createDrawableFromStyle(context, style)
        }
    }

    private fun setupContent() {
        removeAllViews()

        visualContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                (this as MarginLayoutParams).setMargins(visualInsetPx, visualInsetPx, visualInsetPx, visualInsetPx)
            }
        }
        addView(visualContainer)

        val view = LayoutInflater.from(context).inflate(R.layout.keyboard_key, visualContainer, true)

        mainText = view.findViewById(R.id.mainText)
        subText_bottom = view.findViewById(R.id.bottomSubText)
        subText_top = view.findViewById(R.id.topSubText)

        applyStyle()
        updateContent()
        addHoldIndicatorIfNeeded(view)
    }

    private fun updateContent() {
        mainText.apply {
            text = getCurrentMainText()
            setTextColor(style.textColor.toColorInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, style.textSizeSp.value)
            setPadding(0, 0, 0, 0)
            maxLines = 1
            ellipsize = null
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this, 1, style.textSizeSp.value.toInt(), 1, TypedValue.COMPLEX_UNIT_SP
            )
        }

        val firstText = getCurrentSubText()?.toString().orEmpty()
        val secontText = getCurrentSubText_Alt()?.toString().orEmpty()
        val lines = if (MyKeyboardService.isClassic) 2 else 1

        subText_bottom.apply {
            text = firstText
            maxLines = lines
            setLineSpacing(0.75f, 0.75f)
            setTextColor(style.textColor.toColorInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, KeyboardTheme.getHintButtonTextSize(context).value)
            setPadding(0, 0, 0, 0)
            ellipsize = null
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this, 1, KeyboardTheme.getHintButtonTextSize(context).value.toInt(), 1, TypedValue.COMPLEX_UNIT_SP
            )
        }

        subText_top.apply {
            text = secontText
            maxLines = lines
            setLineSpacing(0.75f, 0.75f)
            setTextColor(style.textColor.toColorInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, KeyboardTheme.getHintButtonTextSize(context).value)
            setPadding(0, 0, 0, 0)
            ellipsize = null
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this, 1, KeyboardTheme.getHintButtonTextSize(context).value.toInt(), 1, TypedValue.COMPLEX_UNIT_SP
            )
        }

        if (!MyKeyboardService.showLetterTranscription) {
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

            if (MyKeyboardService.isCaps && key.upperCaseHold != null) {
                hasHoldValue = true
            }
            if (!MyKeyboardService.isCaps && key.lowerCaseHold != null) {
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
                    if (isTouchInBounds && !isLongPressed) {
                        performClick()
                    }
                    handler.removeCallbacks(longPressRunnable)
                    dismissOverlay()
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

        val decor = (context as? InputMethodService)?.window?.window?.decorView ?: return
        val overlayView = createOverlayView().apply {
            layoutParams = LayoutParams(width, height)
        }

        popupWindow = PopupWindow(
            overlayView, (width * 1.25f).toInt(), (height * 1.25f).toInt()
        ).apply {
            isOutsideTouchable = false
            isFocusable = false
            elevation = 24f
            setClippingEnabled(true)
            setTouchInterceptor { _, _ -> false }
        }

        val loc = IntArray(2)
        getLocationInWindow(loc)
        popupWindow?.showAtLocation(
            decor, Gravity.START or Gravity.TOP, loc[0] - ((popupWindow!!.width - width) / 2), loc[1] - popupWindow!!.height - dpToPx(context, 16)
        )
    }

    private fun createOverlayView(): View {
        return LayoutInflater.from(context).inflate(R.layout.keyboard_key, null).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

            findViewById<TextView>(R.id.mainText).apply {
                text = getCurrentMainText()
                setTextColor(style.textColor.toColorInt())
                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, style.textSizeSp.value * resources.displayMetrics.density * 1.5f
                )
                maxLines = 1
                ellipsize = null
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    this, 10, (style.textSizeSp.value * 1.5f).toInt(), 1, TypedValue.COMPLEX_UNIT_SP
                )
            }

            findViewById<TextView>(R.id.bottomSubText).apply {
                val subText = if (isLongPressed) {
                    " " + getCurrentSubText_Hold() + " "
                } else {
                    " " + getCurrentSubText() + " "
                }
                text = subText
                maxLines = 2
                setTextColor(style.textColor.toColorInt())
                setTextSize(
                    TypedValue.COMPLEX_UNIT_SP, KeyboardTheme.getHintButtonTextSize(context).value
                )
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    this, 8, KeyboardTheme.getHintButtonTextSize(context).value.toInt(), 1, TypedValue.COMPLEX_UNIT_SP
                )
            }

            background = KeyboardTheme.createDrawableFromStyle(context, style)
        }
    }

    private fun updateOverlay() {
        val mainText = if (isLongPressed) {
            " " + getCurrentMainText_Hold() + " "
        } else {
            " " + getCurrentMainText() + " "
        }
        val subText = if (isLongPressed) {
            " " + getCurrentSubText_Hold() + " "
        } else {
            " " + getCurrentSubText() + " "
        }

        popupWindow?.contentView?.findViewById<TextView>(R.id.mainText)?.text = mainText
        popupWindow?.contentView?.findViewById<TextView>(R.id.bottomSubText)?.text = subText
        popupWindow?.contentView?.findViewById<TextView>(R.id.topSubText)?.text = ""

        var colorToSet: Int = KeyboardTheme.getColor(1).toColorInt()
        colorToSet = if (MyKeyboardService.isCaps) {
            when {
                key.backgroundColorIndex_uppercase_Hold != null -> KeyboardTheme.getColor(key.backgroundColorIndex_uppercase_Hold!!).toColorInt()

                key.backgroundColorIndex_lowercase != null -> KeyboardTheme.getColor(key.backgroundColorIndex_lowercase!!).toColorInt()

                else -> colorToSet
            }
        } else {
            when {
                key.backgroundColorIndex_lowercase_Hold != null -> KeyboardTheme.getColor(key.backgroundColorIndex_lowercase_Hold!!).toColorInt()

                key.backgroundColorIndex_lowercase != null -> KeyboardTheme.getColor(key.backgroundColorIndex_lowercase!!).toColorInt()

                else -> colorToSet
            }
        }
        popupWindow?.contentView?.setBackgroundColor(colorToSet)
    }

    private fun addHoldIndicatorIfNeeded(root: View) {
        val hasHoldVariant = if (MyKeyboardService.isCaps) {
            key.upperCaseHold != null
        } else {
            key.lowerCaseHold != null
        }

        if (hasHoldVariant) {
            val indicatorSize = dpToPx(context, 4)
            val margin = dpToPx(context, 2)

            val indicator = View(context).apply {
                layoutParams = FrameLayout.LayoutParams(indicatorSize, indicatorSize).apply {
                    gravity = Gravity.TOP or Gravity.END
                    topMargin = margin
                    marginEnd = margin
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(KeyboardTheme.getColor(3).toColorInt())
                    setSize(indicatorSize, indicatorSize)
                }
            }
            (root as ViewGroup).addView(indicator)
        }
    }

    private fun getCurrentMainText(): String? = if (MyKeyboardService.isCaps) key.uppercase else key.lowercase

    private fun getCurrentSubText(): CharSequence? = if (MyKeyboardService.isCaps) key.upperCaseRomanization ?: "" else key.lowerCaseRomanization ?: ""

    private fun getCurrentSubText_Alt(): CharSequence? = if (MyKeyboardService.isCaps) key.upperCaseRomanization_Alt ?: "" else key.lowerCaseRomanization_Alt ?: ""

    private fun getCurrentMainText_Hold(): String? = if (MyKeyboardService.isCaps) key.upperCaseHold else key.lowerCaseHold

    private fun getCurrentSubText_Hold(): CharSequence? = if (MyKeyboardService.isCaps) key.upperCaseHoldHint ?: "" else key.lowerCaseHoldHint ?: ""

    private fun dismissOverlay() {
        try {
            popupWindow?.dismiss()
        } catch (_: Throwable) {
        }
        popupWindow = null
    }

    private fun resetState() {
        handler.postDelayed({
            isLongPressed = false
            applyStyle()
            updateContent()
        }, 0)
    }
}